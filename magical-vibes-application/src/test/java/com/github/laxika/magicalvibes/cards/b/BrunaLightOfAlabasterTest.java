package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BrunaLightOfAlabasterTest extends BaseCardTest {

    /** Bruna on the battlefield, ready to attack. */
    private Permanent addBruna() {
        return addCreatureReady(player1, new BrunaLightOfAlabaster());
    }

    /** Attack with Bruna (index 0) and resolve the attack trigger up to its choice prompt. */
    private void attackWithBruna() {
        declareAttackers(List.of(0));
        harness.passBothPriorities();
    }

    private PendingInteraction.AttachAurasChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.AttachAurasChoice.class);
    }

    private boolean attachedToBruna(Permanent bruna, String auraName) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals(auraName)
                        && bruna.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Attacking puts a chosen Aura card from hand onto the battlefield attached to Bruna")
    void attackAttachesAuraFromHand() {
        Permanent bruna = addBruna();
        HolyStrength holyStrength = new HolyStrength();
        harness.setHand(player1, List.of(holyStrength));

        attackWithBruna();

        assertThat(activeChoice()).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(holyStrength.getId()));

        assertThat(attachedToBruna(bruna, "Holy Strength")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking puts a chosen Aura card from the graveyard onto the battlefield attached to Bruna")
    void attackAttachesAuraFromGraveyard() {
        Permanent bruna = addBruna();
        HolyStrength holyStrength = new HolyStrength();
        harness.setGraveyard(player1, List.of(holyStrength));
        harness.setHand(player1, List.of());

        attackWithBruna();

        harness.handleMultipleCardsChosen(player1, List.of(holyStrength.getId()));

        assertThat(attachedToBruna(bruna, "Holy Strength")).isTrue();
        harness.assertNotInGraveyard(player1, "Holy Strength");
    }

    @Test
    @DisplayName("Attacking moves a chosen Aura already on the battlefield onto Bruna")
    void attackMovesBattlefieldAura() {
        Permanent bruna = addBruna();
        harness.setHand(player1, List.of());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent pacifism = new Permanent(new Pacifism());
        pacifism.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(pacifism);

        attackWithBruna();

        harness.handleMultipleCardsChosen(player1, List.of(pacifism.getCard().getId()));

        assertThat(pacifism.getAttachedTo()).isEqualTo(bruna.getId());
    }

    @Test
    @DisplayName("An Aura that could not enchant Bruna is not offered")
    void auraThatCannotEnchantBrunaIsNotOffered() {
        addBruna();
        AbundantGrowth abundantGrowth = new AbundantGrowth();
        HolyStrength holyStrength = new HolyStrength();
        harness.setHand(player1, List.of(abundantGrowth, holyStrength));
        harness.addToBattlefield(player1, new Forest());

        attackWithBruna();

        List<UUID> offered = activeChoice().validCardIds();
        assertThat(offered).contains(holyStrength.getId());
        assertThat(offered).doesNotContain(abundantGrowth.getId());
    }

    @Test
    @DisplayName("Choosing nothing attaches nothing")
    void choosingNothingAttachesNothing() {
        Permanent bruna = addBruna();
        HolyStrength holyStrength = new HolyStrength();
        harness.setHand(player1, List.of(holyStrength));

        attackWithBruna();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(attachedToBruna(bruna, "Holy Strength")).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Blocking triggers the same Aura attachment")
    void blockingAttachesAura() {
        harness.forceActivePlayer(player2);
        Permanent bruna = addCreatureReady(player1, new BrunaLightOfAlabaster());
        addCreatureReady(player2, new GrizzlyBears());
        HolyStrength holyStrength = new HolyStrength();
        harness.setHand(player1, List.of(holyStrength));

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(activeChoice()).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(holyStrength.getId()));

        assertThat(attachedToBruna(bruna, "Holy Strength")).isTrue();
    }

    @Test
    @DisplayName("With no Auras anywhere the trigger prompts for nothing")
    void noAurasNoPrompt() {
        addBruna();
        harness.setHand(player1, List.of());

        attackWithBruna();

        assertThat(activeChoice()).isNull();
    }
}
