package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AzizaMageTowerCaptain.class, GrizzlyBears.class, LightningBolt.class, AshesToAshes.class})
class AzizaMageTowerCaptainTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting an instant triggers copy ability on the stack")
    void castingInstantTriggersCopyAbility() {
        addCreatureReady(player1, new AzizaMageTowerCaptain());
        addThreeUntappedCreatures(player1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Aziza, Mage Tower Captain"));
    }

    @Test
    @DisplayName("Does not trigger on creature spells")
    void doesNotTriggerOnCreatureSpells() {
        addCreatureReady(player1, new AzizaMageTowerCaptain());
        addThreeUntappedCreatures(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Aziza, Mage Tower Captain"));
    }

    @Test
    @DisplayName("Accepting may prompt and tapping three creatures creates a spell copy")
    void acceptingTapCostCreatesCopy() {
        addCreatureReady(player1, new AzizaMageTowerCaptain());
        addThreeUntappedCreatures(player1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve MayPayTapPermanentsEffect -> may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        tapCreatures(player1, 3);
        harness.passBothPriorities(); // resolve CopyControllerCastSpellEffect

        long boltCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Lightning Bolt"))
                .count();
        assertThat(boltCount).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Declining may prompt does not create a copy")
    void decliningMayPromptDoesNotCopy() {
        addCreatureReady(player1, new AzizaMageTowerCaptain());
        addThreeUntappedCreatures(player1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve MayPayTapPermanentsEffect -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lightning Bolt");
    }

    @Test
    @DisplayName("Accepting without enough untapped creatures does not create a copy")
    void acceptingWithoutEnoughCreaturesDoesNotCopy() {
        addCreatureReady(player1, new AzizaMageTowerCaptain());
        addThreeUntappedCreatures(player1);

        // Leave only Aziza and one Bear untapped, fewer than the required three.
        List<Permanent> creatures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .filter(p -> !p.getCard().getName().equals("Aziza, Mage Tower Captain"))
                .limit(2)
                .toList();
        creatures.forEach(Permanent::tap);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lightning Bolt");
    }

    @Test
    @DisplayName("Aziza and summoning-sick creatures can pay the tap cost")
    void azizaAndSummoningSickCreaturesCanPay() {
        Permanent aziza = harness.addToBattlefieldAndReturn(player1, new AzizaMageTowerCaptain());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        aziza.setSummoningSick(true);
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(aziza.isTapped()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Copying a sorcery with two targets offers new targets")
    void multiTargetSorceryCopyOffersNewTargets() {
        addCreatureReady(player1, new AzizaMageTowerCaptain());
        addThreeUntappedCreatures(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AshesToAshes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        tapCreatures(player1, 3);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void addThreeUntappedCreatures(com.github.laxika.magicalvibes.model.Player player) {
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player, new GrizzlyBears());
        }
    }

    private void tapCreatures(com.github.laxika.magicalvibes.model.Player player, int count) {
        List<Permanent> untapped = gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .filter(p -> !p.isTapped())
                .filter(p -> !p.getCard().getName().equals("Aziza, Mage Tower Captain"))
                .limit(count)
                .toList();
        for (Permanent creature : untapped) {
            harness.handlePermanentChosen(player, creature.getId());
        }
    }
}
