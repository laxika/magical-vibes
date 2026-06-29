package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerControlsPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.HexproofFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnightOfMaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Knight of Malice has hexproof from white and conditional boost effects")
    void hasCorrectEffects() {
        KnightOfMalice card = new KnightOfMalice();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0))
                .isEqualTo(new HexproofFromColorsEffect(Set.of(CardColor.WHITE)));
        assertThat(card.getEffects(EffectSlot.STATIC).get(1))
                .isEqualTo(new AnyPlayerControlsPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                        new StaticBoostEffect(1, 0, GrantScope.SELF)));
    }

    @Test
    @DisplayName("Opponent's white spells cannot target Knight of Malice")
    void opponentWhiteSpellsCannotTarget() {
        harness.addToBattlefield(player2, new KnightOfMalice());

        // Add valid target so spell is playable
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                harness.getPermanentId(player2, "Knight of Malice")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("has hexproof from white");
    }

    @Test
    @DisplayName("Controller's own white spells can target Knight of Malice")
    void controllerWhiteSpellsCanTarget() {
        harness.addToBattlefield(player1, new KnightOfMalice());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0,
                harness.getPermanentId(player1, "Knight of Malice"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack)
                .anyMatch(se -> se.getCard().getName().equals("Pacifism"));
    }

    @Test
    @DisplayName("Non-white spells from opponents can target Knight of Malice")
    void nonWhiteSpellsCanTarget() {
        harness.addToBattlefield(player2, new KnightOfMalice());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Knight of Malice"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack)
                .anyMatch(se -> se.getCard().getName().equals("Shock"));
    }

    @Test
    @DisplayName("Gets +1/+0 when any player controls a white permanent")
    void boostsWhenWhitePermanentExists() {
        harness.addToBattlefield(player1, new KnightOfMalice());
        harness.addToBattlefield(player2, new BenalishKnight());

        GameData gd = harness.getGameData();
        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight of Malice"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3); // 2 base + 1 boost
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2); // no toughness boost
    }

    @Test
    @DisplayName("Gets +1/+0 when controller controls a white permanent")
    void boostsWhenControllerHasWhitePermanent() {
        harness.addToBattlefield(player1, new KnightOfMalice());
        harness.addToBattlefield(player1, new BenalishKnight());

        GameData gd = harness.getGameData();
        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight of Malice"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3); // 2 base + 1 boost
    }

    @Test
    @DisplayName("No boost when no white permanent exists")
    void noBoostWithoutWhitePermanent() {
        harness.addToBattlefield(player1, new KnightOfMalice());
        harness.addToBattlefield(player2, new GrizzlyBears());

        GameData gd = harness.getGameData();
        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight of Malice"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2); // base only
    }

    @Test
    @DisplayName("Boost is +1/+0 regardless of how many white permanents exist")
    void boostDoesNotStackWithMultipleWhitePermanents() {
        harness.addToBattlefield(player1, new KnightOfMalice());
        harness.addToBattlefield(player1, new BenalishKnight());
        harness.addToBattlefield(player2, new BenalishKnight());

        GameData gd = harness.getGameData();
        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight of Malice"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3); // 2 base + 1 (not stacking)
    }
}
