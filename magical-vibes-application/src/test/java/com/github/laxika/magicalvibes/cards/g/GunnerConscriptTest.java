package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AkromasVengeance;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GunnerConscript.class, HolyStrength.class, LeoninScimitar.class, Murder.class, AkromasVengeance.class})
class GunnerConscriptTest extends BaseCardTest {

    @Test
    void getsPlusOnePlusOneForEachAttachedAuraAndEquipment() {
        Permanent gunner = addGunnerReady();
        Permanent aura = addCreatureReady(player1, new HolyStrength());
        Permanent equipment = addCreatureReady(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, gunner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gunner)).isEqualTo(2);

        aura.setAttachedTo(gunner.getId());
        assertThat(gqs.getEffectivePower(gd, gunner)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gunner)).isEqualTo(5);

        equipment.setAttachedTo(gunner.getId());
        assertThat(gqs.getEffectivePower(gd, gunner)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, gunner)).isEqualTo(7);
    }

    @Test
    void createsOneJunkForEachAttachmentCategoryWhenItDies() {
        Permanent gunner = addGunnerReady();
        Permanent aura = addCreatureReady(player1, new HolyStrength());
        Permanent equipment = addCreatureReady(player1, new LeoninScimitar());
        aura.setAttachedTo(gunner.getId());
        equipment.setAttachedTo(gunner.getId());

        destroy(gunner);

        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
    }

    @Test
    void createsNoJunkWhenItDiesWithoutAttachments() {
        Permanent gunner = addGunnerReady();

        destroy(gunner);

        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    void createsOnlyOneJunkForMultipleAurasIncludingAnOpponentsAura() {
        Permanent gunner = addGunnerReady();
        addCreatureReady(player1, new HolyStrength()).setAttachedTo(gunner.getId());
        addCreatureReady(player2, new HolyStrength()).setAttachedTo(gunner.getId());

        assertThat(gqs.getEffectivePower(gd, gunner)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, gunner)).isEqualTo(8);
        destroy(gunner);

        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
        assertThat(countPermanents(player2, "Junk")).isZero();
    }

    @Test
    void createsOnlyOneJunkForMultipleEquipment() {
        Permanent gunner = addGunnerReady();
        addCreatureReady(player1, new LeoninScimitar()).setAttachedTo(gunner.getId());
        addCreatureReady(player1, new LeoninScimitar()).setAttachedTo(gunner.getId());

        assertThat(gqs.getEffectivePower(gd, gunner)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, gunner)).isEqualTo(6);
        destroy(gunner);

        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    @Test
    void stopsCountingEquipmentAfterItMovesToAnotherCreature() {
        Permanent gunner = addGunnerReady();
        Permanent other = addGunnerReady();
        Permanent equipment = addCreatureReady(player1, new LeoninScimitar());
        equipment.setAttachedTo(gunner.getId());
        assertThat(gqs.getEffectivePower(gd, gunner)).isEqualTo(4);

        equipment.setAttachedTo(other.getId());

        assertThat(gqs.getEffectivePower(gd, gunner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gunner)).isEqualTo(2);
        destroy(gunner);
        assertThat(countPermanents(player1, "Junk")).isZero();
    }

    @Test
    void createsBothJunkTokensWhenAttachmentsAreDestroyedSimultaneously() {
        Permanent aura = addCreatureReady(player1, new HolyStrength());
        Permanent equipment = addCreatureReady(player1, new LeoninScimitar());
        Permanent gunner = addGunnerReady();
        aura.setAttachedTo(gunner.getId());
        equipment.setAttachedTo(gunner.getId());
        harness.setHand(player1, List.of(new AkromasVengeance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Gunner Conscript");
        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        assertThat(countPermanents(player1, "Junk")).isEqualTo(2);
    }

    @Test
    void junkSacrificesToExileTopCardAndAllowsCastingWithNormalManaCost() {
        Permanent gunner = addGunnerReady();
        addCreatureReady(player1, new LeoninScimitar()).setAttachedTo(gunner.getId());
        destroy(gunner);
        GunnerConscript topCard = new GunnerConscript();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent junk = findPermanent(player1, "Junk");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(junk), null, null);
        assertThat(countPermanents(player1, "Junk")).isZero();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Gunner Conscript");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void junkCannotBeActivatedOutsideAMainPhase() {
        Permanent gunner = addGunnerReady();
        addCreatureReady(player1, new LeoninScimitar()).setAttachedTo(gunner.getId());
        destroy(gunner);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        Permanent junk = findPermanent(player1, "Junk");

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(junk), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Junk")).isEqualTo(1);
    }

    private Permanent addGunnerReady() {
        return addCreatureReady(player1, new GunnerConscript());
    }

    private void destroy(Permanent gunner) {
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, gunner.getId());
        resolveAllTriggers();
    }
}
