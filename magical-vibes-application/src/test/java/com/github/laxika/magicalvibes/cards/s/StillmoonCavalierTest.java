package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StillmoonCavalierTest extends BaseCardTest {

    private static Card createTargetedInstant(String name, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    private Permanent addCavalierReady(Player player) {
        Permanent perm = new Permanent(new StillmoonCavalier());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    // ===== {W/B}: gains flying =====

    @Test
    @DisplayName("Flying ability grants flying, payable with white or black mana")
    void flyingAbilityGrantsFlying() {
        Permanent cavalier = addCavalierReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOff() {
        Permanent cavalier = addCavalierReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FLYING)).isFalse();
    }

    // ===== {W/B}: gains first strike =====

    @Test
    @DisplayName("First strike ability grants first strike")
    void firstStrikeAbilityGrantsFirstStrike() {
        Permanent cavalier = addCavalierReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== {W/B}{W/B}: +1/+0 =====

    @Test
    @DisplayName("Pump ability gives +1/+0 until end of turn")
    void pumpAbilityBoosts() {
        Permanent cavalier = addCavalierReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(cavalier.getEffectivePower()).isEqualTo(3);
        assertThat(cavalier.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cavalier.getEffectivePower()).isEqualTo(2);
    }

    // ===== Protection =====

    @Test
    @DisplayName("Cannot be targeted by white instant")
    void cannotBeTargetedByWhiteInstant() {
        Permanent cavalier = new Permanent(new StillmoonCavalier());
        cavalier.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(cavalier);

        // Add a legal target so the spell is playable.
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(createTargetedInstant("Blinding Beam", CardColor.WHITE, "{W}")));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, cavalier.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Cannot be targeted by black instant")
    void cannotBeTargetedByBlackInstant() {
        Permanent cavalier = new Permanent(new StillmoonCavalier());
        cavalier.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(cavalier);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(createTargetedInstant("Dark Banishing", CardColor.BLACK, "{B}")));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, cavalier.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by red instant")
    void canBeTargetedByRedInstant() {
        Permanent cavalier = new Permanent(new StillmoonCavalier());
        cavalier.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(cavalier);

        harness.setHand(player1, List.of(createTargetedInstant("Lightning Bolt", CardColor.RED, "{R}")));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, cavalier.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lightning Bolt");
    }
}
