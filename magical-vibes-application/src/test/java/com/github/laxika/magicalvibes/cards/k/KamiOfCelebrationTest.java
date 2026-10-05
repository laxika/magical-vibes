package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DrakeHatchling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfCelebration.class, DrakeHatchling.class, GrizzlyBears.class,
        HolyStrength.class, SwiftfootBoots.class})
class KamiOfCelebrationTest extends BaseCardTest {

    @Test
    @DisplayName("A modified creature attacking exiles the top card and permits playing it")
    void modifiedCreatureAttackExilesTopCard() {
        addCreatureReady(player1, new KamiOfCelebration());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    @DisplayName("Casting a spell from exile puts a counter on a creature you control")
    void castingFromExilePutsCounterOnTargetCreature() {
        addCreatureReady(player1, new KamiOfCelebration());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, topCard.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An unmodified creature attacking does not exile a card")
    void unmodifiedCreatureAttackDoesNotTrigger() {
        addCreatureReady(player1, new KamiOfCelebration());
        addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Each modified attacker triggers separately, including Kami itself")
    void eachModifiedAttackerExilesOneCard() {
        Permanent kami = addCreatureReady(player1, new KamiOfCelebration());
        kami.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new GrizzlyBears());
        Card first = new DrakeHatchling();
        Card second = new DrakeHatchling();
        Card third = new DrakeHatchling();
        harness.setLibrary(player1, List.of(first, second, third));

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("A counter that does not change power or toughness still modifies a creature")
    void chargeCounterMakesAttackerModified() {
        Permanent kami = addCreatureReady(player1, new KamiOfCelebration());
        kami.setCounterCount(CounterType.CHARGE, 1);
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Losing modification after attacking does not stop the trigger")
    void removingCounterAfterAttackDoesNotStopExile() {
        addCreatureReady(player1, new KamiOfCelebration());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card topCard = new DrakeHatchling();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(1));
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Attacking with an empty library exiles nothing and does not cause a loss")
    void emptyLibraryDoesNotCauseLoss() {
        Permanent kami = addCreatureReady(player1, new KamiOfCelebration());
        kami.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Casting from hand does not trigger the counter ability")
    void castingFromHandDoesNotAddCounter() {
        Permanent kami = addCreatureReady(player1, new KamiOfCelebration());

        harness.castFromHand(player1, new DrakeHatchling(), "{2}{U}");
        resolveAllTriggers();

        assertThat(kami.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Drake Hatchling");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Exile-cast trigger can target Kami but not an opponent's creature or the creature spell")
    void exileCastTargetsOnlyCreaturesYouControlOnBattlefield() {
        Permanent kami = addCreatureReady(player1, new KamiOfCelebration());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        Card spell = new DrakeHatchling();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castFromExile(player1, spell.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(kami.getId()).doesNotContain(opponent.getId(), spell.getId());
        harness.handlePermanentChosen(player1, kami.getId());
        harness.passBothPriorities();

        assertThat(kami.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Drake Hatchling");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Drake Hatchling");
    }

    @Test
    @DisplayName("An Aura you control makes the attacker modified")
    void ownAuraModifiesAttacker() {
        Permanent kami = addCreatureReady(player1, new KamiOfCelebration());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(kami.getId());
        Card topCard = new KamiOfCelebration();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An opponent's Aura does not make your attacker modified")
    void opponentsAuraDoesNotModifyAttacker() {
        Permanent kami = addCreatureReady(player1, new KamiOfCelebration());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(kami.getId());
        Card topCard = new KamiOfCelebration();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Equipment makes the attacker modified even if an opponent controls it")
    void opponentsEquipmentModifiesAttacker() {
        Permanent kami = addCreatureReady(player1, new KamiOfCelebration());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new SwiftfootBoots());
        equipment.setAttachedTo(kami.getId());
        Card topCard = new KamiOfCelebration();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

}
