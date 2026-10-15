package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MineshaftSpider;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DireFlail.class, DireBlunderbuss.class, DarksteelRelic.class, GrizzlyBears.class,
        MineshaftSpider.class})
class DireFlailTest extends BaseCardTest {

    @Test
    @DisplayName("Dire Flail boosts its equipped creature")
    void direFlailBoostsEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent flail = addCreatureReady(player1, new DireFlail());
        flail.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Craft exiles another artifact and returns Dire Flail transformed")
    void craftReturnsTransformed() {
        Permanent flail = addCreatureReady(player1, new DireFlail());
        Permanent material = addCreatureReady(player1, new DarksteelRelic());
        addCraftMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(flail, material);
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof DireBlunderbuss);
    }

    @Test
    @DisplayName("Craft exiles an artifact card from the graveyard and returns transformed")
    void craftReturnsTransformedWithGraveyardArtifact() {
        Permanent flail = addCreatureReady(player1, new DireFlail());
        DarksteelRelic material = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(material));
        addCraftMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(flail);
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof DireBlunderbuss);
    }

    @Test
    @DisplayName("Dire Blunderbuss may sacrifice another artifact and deal damage equal to the equipped creature's power")
    void backFaceAttackTriggerSacrificesArtifactAndDealsPowerDamage() {
        Permanent blunderbuss = addTransformedBlunderbuss();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        blunderbuss.setAttachedTo(creature.getId());
        Permanent material = addCreatureReady(player1, new DarksteelRelic());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice sacrificeChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(sacrificeChoice.validIds()).containsExactly(material.getId());
        harness.handlePermanentChosen(player1, material.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Darksteel Relic");
        assertThat(blunderbuss.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Declining Dire Blunderbuss's attack trigger does nothing")
    void decliningBackFaceAttackTriggerDoesNothing() {
        Permanent blunderbuss = addTransformedBlunderbuss();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        blunderbuss.setAttachedTo(creature.getId());
        Permanent material = addCreatureReady(player1, new DarksteelRelic());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(material);
    }

    @Test
    void frontFaceEquipAttachesAndBoostsOnlyTheChosenCreature() {
        Permanent flail = addCreatureReady(player1, new DireFlail());
        Permanent creature = addCreatureReady(player1, new MineshaftSpider());
        Permanent other = addCreatureReady(player1, new MineshaftSpider());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(flail.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
    }

    @Test
    void backFaceEquipAttachesAndBoostsCreature() {
        Permanent blunderbuss = addTransformedBlunderbuss();
        Permanent creature = addCreatureReady(player1, new MineshaftSpider());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blunderbuss.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void craftCannotUseTheFlailItselfAsItsOnlyMaterial() {
        Permanent flail = addCreatureReady(player1, new DireFlail());
        addCraftMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(flail);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void craftCannotUseAnOpponentsArtifact() {
        Permanent flail = addCreatureReady(player1, new DireFlail());
        Permanent material = addCreatureReady(player2, new DireFlail());
        addCraftMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(flail);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(material);
    }

    @Test
    void craftCannotBeActivatedDuringCombat() {
        Permanent flail = addCreatureReady(player1, new DireFlail());
        Permanent material = addCreatureReady(player1, new DireFlail());
        addCraftMana();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(flail, material);
    }

    @Test
    void craftExilesCostsBeforeResolutionAndReturnsUnattached() {
        Permanent creature = addCreatureReady(player1, new MineshaftSpider());
        Permanent flail = addCreatureReady(player1, new DireFlail());
        flail.setAttachedTo(creature.getId());
        Permanent material = addCreatureReady(player1, new DireFlail());
        addCraftMana();

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(flail, material);
        assertThat(gd.findExiledCard(flail.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Dire Blunderbuss");
        assertThat(returned.isTransformed()).isTrue();
        assertThat(returned.getAttachedTo()).isNull();
        assertThat(gd.findExiledCard(flail.getCard().getId())).isNull();
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
    }

    @Test
    void attackTriggerAllowsSacrificingAnotherDireBlunderbuss() {
        Permanent blunderbuss = addTransformedBlunderbuss();
        Permanent creature = addCreatureReady(player1, new MineshaftSpider());
        blunderbuss.setAttachedTo(creature.getId());
        Permanent otherBlunderbuss = addTransformedBlunderbuss();
        addCreatureReady(player1, new DireFlail());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(otherBlunderbuss.getId()).doesNotContain(blunderbuss.getId());
    }

    @Test
    void reflexiveDamageUsesCreaturePowerWhenItResolves() {
        Permanent blunderbuss = addTransformedBlunderbuss();
        Permanent creature = addCreatureReady(player1, new MineshaftSpider());
        blunderbuss.setAttachedTo(creature.getId());
        Permanent material = addCreatureReady(player1, new DireFlail());
        Permanent target = addCreatureReady(player2, new MineshaftSpider());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, material.getId());
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).hasSize(1);
        blunderbuss.setAttachedTo(null);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Mineshaft Spider");
        harness.assertInGraveyard(player1, "Dire Flail");
    }

    private void addCraftMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addTransformedBlunderbuss() {
        DireFlail front = new DireFlail();
        Permanent blunderbuss = new Permanent(front);
        blunderbuss.setCard(front.getBackFaceCard());
        blunderbuss.setTransformed(true);
        blunderbuss.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(blunderbuss);
        return blunderbuss;
    }

}
