package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.e.EnterTheInfinite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZaffaiThunderConductor.class, Shock.class, JacesIngenuity.class,
        EnterTheInfinite.class, BarkshellBlessing.class, GrizzlyBears.class})
class ZaffaiThunderConductorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant triggers scry 1")
    void castingInstantTriggersScry() {
        addZaffai();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

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
