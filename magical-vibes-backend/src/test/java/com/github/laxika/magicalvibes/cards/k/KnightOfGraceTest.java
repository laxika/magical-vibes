package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerControlsColorConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.HexproofFromColorsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnightOfGraceTest extends BaseCardTest {

    @Test
    @DisplayName("Knight of Grace has hexproof from black and conditional boost effects")
    void hasCorrectEffects() {
        KnightOfGrace card = new KnightOfGrace();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0))
                .isInstanceOf(HexproofFromColorsEffect.class);
        assertThat(((HexproofFromColorsEffect) card.getEffects(EffectSlot.STATIC).get(0)).colors())
                .containsExactly(CardColor.BLACK);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1))
                .isInstanceOf(AnyPlayerControlsColorConditionalEffect.class);
    }

    @Test
    @DisplayName("Black spells from opponent cannot target Knight of Grace")
    void blackSpellsFromOpponentCannotTarget() {
        harness.addToBattlefield(player1, new KnightOfGrace());

        // Add valid target so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, harness.getPermanentId(player1, "Knight of Grace")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof from black");
    }

    @Test
    @DisplayName("Non-black spells can target Knight of Grace")
    void nonBlackSpellsCanTarget() {
        harness.addToBattlefield(player2, new KnightOfGrace());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Knight of Grace"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack)
                .anyMatch(se -> se.getCard().getName().equals("Shock"));
    }

    @Test
    @DisplayName("Controller's own black spells can target Knight of Grace")
    void ownBlackSpellsCanTarget() {
        harness.addToBattlefield(player1, new KnightOfGrace());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        // Controller targets their own Knight — hexproof from black doesn't block own spells
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Knight of Grace"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack)
                .anyMatch(se -> se.getCard().getName().equals("Terror"));
    }

    @Test
    @DisplayName("Black activated abilities from opponent cannot target Knight of Grace")
    void blackActivatedAbilitiesFromOpponentCannotTarget() {
        Permanent knight = new Permanent(new KnightOfGrace());
        knight.setSummoningSick(false);
        knight.tap(); // Royal Assassin requires tapped target
        harness.getGameData().playerBattlefields.get(player1.getId()).add(knight);

        // Add valid target so ability is usable
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bears);

        Permanent assassin = new Permanent(new RoyalAssassin());
        assassin.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(assassin);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, knight.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gets +1/+0 when any player controls a black permanent")
    void boostWhenAnyPlayerControlsBlackPermanent() {
        harness.addToBattlefield(player1, new KnightOfGrace());

        Permanent knightPerm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight of Grace"))
                .findFirst().orElseThrow();

        // No black permanent — no bonus
        var bonus = gqs.computeStaticBonus(gd, knightPerm);
        assertThat(bonus.power()).isEqualTo(0);

        // Add a black permanent to opponent's battlefield
        harness.addToBattlefield(player2, new RoyalAssassin());

        bonus = gqs.computeStaticBonus(gd, knightPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +1/+0 when controller controls a black permanent")
    void boostWhenControllerControlsBlackPermanent() {
        harness.addToBattlefield(player1, new KnightOfGrace());

        Permanent knightPerm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight of Grace"))
                .findFirst().orElseThrow();

        // No black permanent — no bonus
        var bonus = gqs.computeStaticBonus(gd, knightPerm);
        assertThat(bonus.power()).isEqualTo(0);

        // Add a black permanent to controller's own battlefield
        harness.addToBattlefield(player1, new RoyalAssassin());

        bonus = gqs.computeStaticBonus(gd, knightPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost when no black permanents exist")
    void noBoostWithoutBlackPermanents() {
        harness.addToBattlefield(player1, new KnightOfGrace());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent knightPerm = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight of Grace"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, knightPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }
}
