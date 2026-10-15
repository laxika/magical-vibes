package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Decomposition;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.m.MomentaryBlink;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.w.WardOfLights;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Decomposition.class, HakimLoreweaver.class, IronTuskElephant.class, Pacifism.class,
        WardOfLights.class, MomentaryBlink.class})
class HakimLoreweaverTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep ability returns the targeted Aura from the graveyard attached to Hakim")
    void upkeepAbilityReturnsAuraAttachedToHakim() {
        Permanent hakim = addHakim(player1);
        Card aura = new Pacifism();
        addToGraveyard(player1, aura);
        beginUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, aura.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotInGraveyard(player1, "Pacifism");
        Permanent returnedAura = findPermanent(player1, "Pacifism");
        assertThat(returnedAura.getAttachedTo()).isEqualTo(hakim.getId());
    }

    @Test
    @DisplayName("Upkeep ability can't be activated outside the controller's upkeep")
    void upkeepAbilityRejectedOutsideUpkeep() {
        addHakim(player1);
        Card aura = new Pacifism();
        addToGraveyard(player1, aura);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, aura.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Upkeep ability can't be activated during an opponent's upkeep")
    void upkeepAbilityRejectedDuringOpponentsUpkeep() {
        addHakim(player1);
        Card aura = new Pacifism();
        addToGraveyard(player1, aura);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, aura.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Upkeep ability can't be activated while Hakim is enchanted")
    void upkeepAbilityRejectedWhileEnchanted() {
        Permanent hakim = addHakim(player1);
        attachAura(player1, new Pacifism(), hakim);
        Card aura = new Pacifism();
        addToGraveyard(player1, aura);
        beginUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, aura.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability destroys only the Auras attached to Hakim")
    void tapAbilityDestroysAurasAttachedToHakim() {
        Permanent hakim = addHakim(player1);
        Permanent hakimAura = attachAura(player1, new Pacifism(), hakim);
        Permanent opponentAura = attachAura(player2, new Pacifism(), hakim);
        Permanent elephant = addCreatureReady(player1, new IronTuskElephant());
        Permanent survivingPacifism = attachAura(player1, new Pacifism(), elephant);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(hakim.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(hakimAura.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(opponentAura.getId()));
        assertThat(survivingPacifism.getAttachedTo()).isEqualTo(elephant.getId());
        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertInGraveyard(player2, "Pacifism");
        assertThat(findPermanents(player1, "Pacifism")).containsExactly(survivingPacifism);
    }

    @Test
    @DisplayName("Upkeep ability leaves an Aura in the graveyard when it cannot enchant Hakim")
    void upkeepAbilityLeavesIllegalAuraInGraveyard() {
        addHakim(player1);
        Card aura = new Decomposition();
        addToGraveyard(player1, aura);
        beginUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, aura.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Decomposition");
        harness.assertNotOnBattlefield(player1, "Decomposition");
    }

    @Test
    @DisplayName("Upkeep ability may be activated again before the first Aura returns")
    void upkeepAbilityCanBeActivatedMultipleTimesBeforeResolution() {
        Permanent hakim = addHakim(player1);
        Card firstAura = new Pacifism();
        Card secondAura = new Pacifism();
        addToGraveyard(player1, firstAura);
        addToGraveyard(player1, secondAura);
        beginUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 0, null, firstAura.getId(), Zone.GRAVEYARD);
        harness.activateAbility(player1, 0, 0, null, secondAura.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Pacifism"))
                .hasSize(2)
                .allSatisfy(aura -> assertThat(aura.getAttachedTo()).isEqualTo(hakim.getId()));
    }

    @Test
    void returningAuraDoesNotFollowHakimThroughBlink() {
        Permanent hakim = addHakim(player1);
        Card aura = new Pacifism();
        harness.setGraveyard(player1, List.of(aura));
        beginUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player1, List.of(new MomentaryBlink()));

        harness.activateAbility(player1, 0, 0, null, aura.getId(), Zone.GRAVEYARD);
        harness.castInstant(player1, 0, hakim.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Hakim, Loreweaver").getId()).isNotEqualTo(hakim.getId());
        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertNotOnBattlefield(player1, "Pacifism");
    }

    @Test
    void destructionAbilityDoesNotFollowHakimThroughBlink() {
        Permanent hakim = addHakim(player1);
        beginUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player1, List.of(new MomentaryBlink()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.castInstant(player1, 0, hakim.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent returnedHakim = findPermanent(player1, "Hakim, Loreweaver");
        assertThat(returnedHakim.getId()).isNotEqualTo(hakim.getId());
        Permanent newAura = attachAura(player1, new Pacifism(), returnedHakim);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Pacifism")).containsExactly(newAura);
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    void returningAuraCannotEnterWhenHakimGainsProtection() {
        Permanent hakim = addHakim(player1);
        Card aura = new Pacifism();
        harness.setGraveyard(player1, List.of(aura));
        beginUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, aura.getId(), Zone.GRAVEYARD);
        Permanent ward = attachAura(player1, new WardOfLights(), hakim);
        ward.setChosenColor(CardColor.WHITE);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertNotOnBattlefield(player1, "Pacifism");
        assertThat(gd.permanentsEnteredBattlefieldThisTurn.getOrDefault(player1.getId(), List.of()))
                .doesNotContain(aura);
    }

    @Test
    void upkeepAbilityRejectsNonAuraCard() {
        addHakim(player1);
        Card creature = new IronTuskElephant();
        harness.setGraveyard(player1, List.of(creature));
        beginUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null,
                creature.getId(), Zone.GRAVEYARD)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void upkeepAbilityRejectsOpponentsGraveyard() {
        addHakim(player1);
        Card aura = new Pacifism();
        harness.setGraveyard(player2, List.of(aura));
        beginUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null,
                aura.getId(), Zone.GRAVEYARD)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void upkeepAbilityDoesNothingWhenTargetLeavesGraveyard() {
        addHakim(player1);
        Card aura = new Pacifism();
        harness.setGraveyard(player1, List.of(aura));
        beginUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, aura.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(aura));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Pacifism");
        assertThat(gd.findExiledCard(aura.getId())).isNotNull();
    }

    private Permanent addHakim(Player player) {
        return addCreatureReady(player, new HakimLoreweaver());
    }

    private Permanent attachAura(Player player, Card auraCard, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, auraCard);
        aura.setAttachedTo(host.getId());
        return aura;
    }

    private void beginUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UPKEEP);
    }

    private void addToGraveyard(Player player, Card card) {
        harness.getGameData().playerGraveyards.get(player.getId()).add(card);
    }

}
