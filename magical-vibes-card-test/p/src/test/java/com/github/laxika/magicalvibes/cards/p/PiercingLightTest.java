package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PiercingLight.class, AirElemental.class, GrizzlyBears.class})
class PiercingLightTest extends BaseCardTest {

    @Test
    void damagesAttackingCreatureAndScriesOne() {
        Permanent attacker = addCombatCreature(player2, new AirElemental(), true);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        castSpellAt(attacker.getId());

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damagesBlockingCreatureAndScriesOne() {
        Permanent blocker = addCombatCreature(player2, new AirElemental(), false);

        castSpellAt(blocker.getId());

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PiercingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    void canPutScryedCardOnBottomWithoutReorderingOtherCards() {
        Permanent attacker = addCombatCreature(player2, new AirElemental(), true);
        Card topCard = new GrizzlyBears();
        Card nextCard = new AirElemental();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        castSpellAt(attacker.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canKeepScryedCardOnTop() {
        Permanent attacker = addCombatCreature(player2, new AirElemental(), true);
        Card topCard = new GrizzlyBears();
        Card nextCard = new AirElemental();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        castSpellAt(attacker.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotScryWhenTargetStopsAttackingBeforeResolution() {
        Permanent attacker = addCombatCreature(player2, new AirElemental(), true);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new PiercingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lethalDamageStillAllowsScry() {
        Permanent attacker = addCombatCreature(player2, new GrizzlyBears(), true);
        Card topCard = new AirElemental();
        harness.setLibrary(player1, List.of(topCard));

        castSpellAt(attacker.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(attacker.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dealsDamageWithAnEmptyLibrary() {
        Permanent attacker = addCombatCreature(player2, new AirElemental(), true);
        harness.setLibrary(player1, List.of());

        castSpellAt(attacker.getId());

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castSpellAt(UUID targetId) {
        harness.setHand(player1, List.of(new PiercingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addCombatCreature(Player owner, Card card, boolean attacking) {
        Permanent permanent = harness.addToBattlefieldAndReturn(owner, card);
        permanent.setSummoningSick(false);
        if (attacking) {
            permanent.setAttacking(true);
            permanent.setAttackTarget(player1.getId());
        } else {
            permanent.setBlocking(true);
            permanent.addBlockingTargetId(UUID.randomUUID());
        }
        return permanent;
    }
}
