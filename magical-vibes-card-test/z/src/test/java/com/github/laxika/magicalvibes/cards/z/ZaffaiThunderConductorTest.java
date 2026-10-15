package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.e.EnterTheInfinite;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.r.Reverberate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZaffaiThunderConductor.class, Shock.class, JacesIngenuity.class,
        EnterTheInfinite.class, BarkshellBlessing.class, GrizzlyBears.class,
        LeylineOfSanctity.class, Fireball.class, Reverberate.class})
class ZaffaiThunderConductorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant triggers scry 1")
    void castingInstantTriggersScry() {
        addZaffai();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Casting an instant with mana value 5 creates a 4/4 blue-red Elemental")
    void highManaValueSpellCreatesElemental() {
        addZaffai();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        resolveZaffaiTriggers();

        assertThat(countPermanents(player1, "Elemental")).isOne();
        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a spell with mana value 10 deals 10 damage to the opponent")
    void veryHighManaValueSpellDealsTenDamage() {
        addZaffai();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new EnterTheInfinite()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0);
        resolveZaffaiTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("The random damage does not target and can damage a hexproof opponent")
    void randomDamageDoesNotTarget() {
        addZaffai();
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new EnterTheInfinite()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0);
        resolveZaffaiTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Copying an instant triggers Zaffai")
    void copyingInstantTriggersZaffai() {
        addZaffai();
        Permanent conspireA = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent conspireB = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        int scryCount = 0;
        int guard = 0;
        while ((!gd.stack.isEmpty() || gd.interaction.activeInteraction() != null) && guard++ < 20) {
            PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
            if (scry != null) {
                scryCount++;
                harness.getGameService().handleInteractionAnswer(gd, player1,
                        new InteractionAnswer.ScryOrder(List.of(0), List.of()));
            } else {
                harness.passBothPriorities();
            }
        }

        assertThat(scryCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Scry precedes token creation within the same magecraft resolution")
    void scryPrecedesTokenCreation() {
        addZaffai();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(countPermanents(player1, "Elemental")).isZero();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(countPermanents(player1, "Elemental")).isOne();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A large spell scries, creates an Elemental, then deals damage in one resolution")
    void allMagecraftInstructionsResolveTogetherInOrder() {
        addZaffai();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new EnterTheInfinite()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(countPermanents(player1, "Elemental")).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(countPermanents(player1, "Elemental")).isOne();
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(gd.stack).hasSize(1);
    }

    @ParameterizedTest
    @CsvSource({"0, 0, 20", "3, 0, 20", "4, 1, 20", "8, 1, 20", "9, 1, 10"})
    @DisplayName("The chosen X counts toward both magecraft mana-value thresholds")
    void chosenXCountsTowardManaValue(int x, int tokens, int opponentLife) {
        addZaffai();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, x + 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, x, player2.getId());
        resolveZaffaiTriggers();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(tokens);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger magecraft")
    void opponentSpellDoesNotTrigger() {
        addZaffai();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Casting a creature does not trigger magecraft")
    void creatureSpellDoesNotTrigger() {
        addZaffai();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Grizzly Bears")).isOne();
    }

    @ParameterizedTest
    @CsvSource({"4, 20", "9, 10"})
    @DisplayName("Copying an opponent's X spell uses its mana value without casting the copy")
    void copyingOpponentSpellUsesChosenX(int x, int opponentLife) {
        addZaffai();
        Fireball original = new Fireball();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(original));
        harness.setHand(player1, List.of(new Reverberate()));
        harness.addMana(player2, ManaColor.RED, x + 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, x, player1.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, original.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(countPermanents(player1, "Elemental")).isZero();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(countPermanents(player1, "Elemental")).isOne();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(2);
    }

    private void addZaffai() {
        harness.addToBattlefield(player1, new ZaffaiThunderConductor());
    }

    private void resolveZaffaiTriggers() {
        int guard = 0;
        while ((!gd.stack.isEmpty() || gd.interaction.activeInteraction() != null) && guard++ < 20) {
            if (gd.stack.size() == 1 && gd.interaction.activeInteraction() == null) {
                return;
            }
            PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
            if (scry != null) {
                harness.getGameService().handleInteractionAnswer(gd, player1,
                        new InteractionAnswer.ScryOrder(List.of(0), List.of()));
            } else {
                harness.passBothPriorities();
            }
        }
    }
}
