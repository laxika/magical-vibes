package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.w.WhiteWard;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrunaLightOfAlabaster.class, AbundantGrowth.class, Forest.class, GrizzlyBears.class,
        HolyStrength.class, Pacifism.class, WhiteWard.class, ControlMagic.class})
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
        Permanent pacifism = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        pacifism.setAttachedTo(bears.getId());

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
        Permanent bruna = addCreatureReady(player1, new BrunaLightOfAlabaster());
        addCreatureReady(player2, new GrizzlyBears());
        HolyStrength holyStrength = new HolyStrength();
        harness.setHand(player1, List.of(holyStrength));

        declareAttackersAndPrepareBlockers(player2, List.of(0));
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

    @Test
    @DisplayName("One resolution can attach Auras from the battlefield, hand, and graveyard")
    void attachesAurasFromAllThreeZones() {
        Permanent bruna = addBruna();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent battlefieldAura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        battlefieldAura.setAttachedTo(bears.getId());
        HolyStrength handAura = new HolyStrength();
        HolyStrength graveyardAura = new HolyStrength();
        harness.setHand(player1, List.of(handAura));
        harness.setGraveyard(player1, List.of(graveyardAura));

        attackWithBruna();
        harness.handleMultipleCardsChosen(player1, List.of(
                battlefieldAura.getCard().getId(), handAura.getId(), graveyardAura.getId()));

        assertThat(battlefieldAura.getAttachedTo()).isEqualTo(bruna.getId());
        assertThat(findPermanents(player1, "Holy Strength"))
                .hasSize(2).allMatch(p -> bruna.getId().equals(p.getAttachedTo()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Holy Strength");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(battlefieldAura);
    }

    @Test
    @DisplayName("Only the chosen subset enters, and opposing hands and graveyards are excluded")
    void choosesSubsetOnlyFromOwnHandAndGraveyard() {
        Permanent bruna = addBruna();
        HolyStrength chosen = new HolyStrength();
        HolyStrength declined = new HolyStrength();
        HolyStrength opposingHand = new HolyStrength();
        HolyStrength opposingGraveyard = new HolyStrength();
        harness.setHand(player1, List.of(chosen, declined));
        harness.setHand(player2, List.of(opposingHand));
        harness.setGraveyard(player2, List.of(opposingGraveyard));

        attackWithBruna();

        assertThat(activeChoice().validCardIds()).contains(chosen.getId(), declined.getId())
                .doesNotContain(opposingHand.getId(), opposingGraveyard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(attachedToBruna(bruna, "Holy Strength")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(declined);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opposingHand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingGraveyard);
    }

    @Test
    @DisplayName("An Aura already attached to Bruna stays attached without another choice")
    void alreadyAttachedAuraDoesNotMoveAgain() {
        Permanent bruna = addBruna();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(bruna.getId());
        long timestamp = aura.getTimestamp();
        harness.setHand(player1, List.of());

        attackWithBruna();

        assertThat(activeChoice()).isNull();
        assertThat(aura.getAttachedTo()).isEqualTo(bruna.getId());
        assertThat(aura.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("Simultaneous battlefield attachments do not block each other through protection")
    void battlefieldAurasAttachSimultaneouslyBeforeProtectionCleanup() {
        Permanent bruna = addBruna();
        Permanent firstBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent ward = harness.addToBattlefieldAndReturn(player1, new WhiteWard());
        ward.setAttachedTo(firstBears.getId());
        Permanent strength = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        strength.setAttachedTo(secondBears.getId());
        harness.setHand(player1, List.of());

        attackWithBruna();
        harness.handleMultipleCardsChosen(player1, List.of(ward.getCard().getId(), strength.getCard().getId()));

        assertThat(ward.getAttachedTo()).isEqualTo(bruna.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(strength);
        harness.assertInGraveyard(player2, "Holy Strength");
    }

    @Test
    @DisplayName("Simultaneous control Auras give the nonactive player's Aura the later timestamp")
    void blockingOrdersOpposingControlAurasInActivePlayerOrder() {
        Permanent bruna = addBruna();
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent attackingBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownControl = harness.addToBattlefieldAndReturn(player1, new ControlMagic());
        ownControl.setAttachedTo(ownBears.getId());
        Permanent opposingControl = harness.addToBattlefieldAndReturn(player2, new ControlMagic());
        opposingControl.setAttachedTo(attackingBears.getId());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(
                opposingControl.getCard().getId(), ownControl.getCard().getId()));

        assertThat(ownControl.getAttachedTo()).isEqualTo(bruna.getId());
        assertThat(opposingControl.getAttachedTo()).isEqualTo(bruna.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bruna);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bruna);
    }
}
