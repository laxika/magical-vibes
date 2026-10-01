package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeaveTheNightmare.class, GrizzlyBears.class, Island.class, Shock.class})
class WeaveTheNightmareTest extends BaseCardTest {

    @Test
    @DisplayName("Heist mode offers random nonland cards from the target opponent's library")
    void heistMode() {
        Card land = new Island();
        Card creature = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player2, List.of(land, creature, shock));
        harness.setHand(player1, List.of(new WeaveTheNightmare()));
        addMana(player1);

        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(player2.getId()));
        harness.passBothPriorities();

        PendingInteraction.HeistCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HeistCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(2).allMatch(card -> !card.hasType(CardType.LAND));

        Card chosen = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .contains(chosen.getId());
        assertThat(gd.exilePlayPermissions.get(chosen.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Creature mode gives -5/-5 until end of turn")
    void creatureMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WeaveTheNightmare()));
        addMana(player1);

        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{1}, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counter mode counters a noncreature spell")
    void counterMode() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player2.getId());

        harness.setHand(player1, List.of(new WeaveTheNightmare()));
        addMana(player1);
        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{2}, shock.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("The second mode is unavailable without a nonland permanent you do not own")
    void cannotChooseTwoWithoutStolenPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WeaveTheNightmare()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(player2.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional modal modes");
    }

    @Test
    @DisplayName("A controlled permanent you do not own enables choosing two modes")
    void canChooseTwoWithStolenPermanent() {
        Permanent stolenCreature = addStolenCreature();
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player2.getId());

        harness.setHand(player1, List.of(new WeaveTheNightmare()));
        addMana(player1);
        harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1, 2}, shock.getId(), List.of(stolenCreature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private Permanent addStolenCreature() {
        GrizzlyBears ownedByPlayer2 = new GrizzlyBears();
        ownedByPlayer2.setOwnerId(player2.getId());
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player2, ownedByPlayer2);
        gd.stolenCreatures.put(stolenCreature.getId(), player2.getId());
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player1.getId(), stolenCreature,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        EffectDuration.PERMANENT, null, "Test setup"));
        return stolenCreature;
    }
}
