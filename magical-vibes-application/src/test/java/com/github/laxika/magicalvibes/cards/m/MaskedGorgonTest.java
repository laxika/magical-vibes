package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AnuridBrushhopper;
import com.github.laxika.magicalvibes.cards.e.EpicStruggle;
import com.github.laxika.magicalvibes.cards.h.HaplessResearcher;
import com.github.laxika.magicalvibes.cards.i.IronshellBeetle;
import com.github.laxika.magicalvibes.cards.l.Lifelace;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.x.XathridGorgon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnuridBrushhopper.class, EpicStruggle.class, HaplessResearcher.class, IronshellBeetle.class, Lifelace.class, MaskedGorgon.class, NamelessInversion.class, SuntailHawk.class, XathridGorgon.class})
class MaskedGorgonTest extends BaseCardTest {

    @Test
    @DisplayName("Green and white creatures have protection from Gorgons")
    void grantsProtectionFromGorgonsToGreenAndWhiteCreatures() {
        harness.addToBattlefield(player1, new MaskedGorgon());
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new IronshellBeetle());
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent greenWhiteCreature = harness.addToBattlefieldAndReturn(player1, new AnuridBrushhopper());
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new HaplessResearcher());
        Permanent greenEnchantment = harness.addToBattlefieldAndReturn(player1, new EpicStruggle());
        Permanent gorgon = harness.addToBattlefieldAndReturn(player2, new XathridGorgon());

        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, greenCreature, gorgon)).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, whiteCreature, gorgon)).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, greenWhiteCreature, gorgon)).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, blueCreature, gorgon)).isFalse();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, greenEnchantment, gorgon)).isFalse();
    }

    @Test
    @DisplayName("Threshold grants protection from green and white")
    void thresholdGrantsProtectionFromGreenAndWhite() {
        Permanent maskedGorgon = addMaskedGorgon(player1);

        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.WHITE)).isFalse();

        fillGraveyard(player1, 6);

        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.BLUE)).isFalse();

        fillGraveyard(player1, 7);

        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.BLUE)).isFalse();

        fillGraveyard(player1, 6);

        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("The threshold only counts the controller's graveyard")
    void thresholdDoesNotCountOpponentsGraveyard() {
        Permanent maskedGorgon = addMaskedGorgon(player1);
        fillGraveyard(player2, 7);

        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, maskedGorgon, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("A color-changed Masked Gorgon is included in its global protection effect")
    void colorChangedMaskedGorgonProtectsItselfFromGorgons() {
        Permanent maskedGorgon = addMaskedGorgon(player1);
        Permanent gorgon = harness.addToBattlefieldAndReturn(player2, new XathridGorgon());

        harness.setHand(player1, List.of(new Lifelace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, maskedGorgon.getId());

        assertThat(gqs.getEffectiveColors(gd, maskedGorgon)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, maskedGorgon, gorgon)).isTrue();
    }

    private Permanent addMaskedGorgon(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MaskedGorgon());
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            cards.add(new SuntailHawk());
        }
        harness.setGraveyard(player, cards);
    }

    @Test
    @DisplayName("A green Masked Gorgon has protection from Gorgons")
    void greenMaskedGorgonHasProtectionFromGorgons() {
        Permanent maskedGorgon = addMaskedGorgon(player1);

        harness.setHand(player1, List.of(new Lifelace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, maskedGorgon.getId());

        assertThat(gqs.getEffectiveColors(gd, maskedGorgon)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, maskedGorgon, maskedGorgon)).isTrue();
    }

    @Test
    @DisplayName("A changeling spell cannot target a green creature protected from Gorgons")
    void changelingSpellCannotTargetGreenCreature() {
        harness.addToBattlefield(player1, new MaskedGorgon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AnuridBrushhopper());
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A changeling spell cannot target a white creature protected from Gorgons")
    void changelingSpellCannotTargetWhiteCreature() {
        harness.addToBattlefield(player1, new MaskedGorgon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A changeling spell loses its target when that creature becomes green")
    void changelingSpellTargetBecomesProtectedBeforeResolution() {
        harness.addToBattlefield(player1, new MaskedGorgon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HaplessResearcher());
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());

        harness.setHand(player2, List.of(new Lifelace()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hapless Researcher");
        harness.assertInGraveyard(player1, "Nameless Inversion");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Protection from Gorgons prevents targeting by a Gorgon's activated ability")
    void gorgonAbilityCannotTargetProtectedCreature() {
        harness.addToBattlefield(player2, new MaskedGorgon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AnuridBrushhopper());
        addCreatureReady(player1, new XathridGorgon());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gorgons cannot block protected creatures even below threshold")
    void gorgonCannotBlockProtectedCreature() {
        harness.addToBattlefield(player1, new MaskedGorgon());
        addCreatureReady(player1, new AnuridBrushhopper());
        addCreatureReady(player2, new MaskedGorgon());
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, Map.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection from Gorgons prevents combat damage from an attacking Masked Gorgon")
    void protectedBlockerSurvivesGorgonCombatDamage() {
        addCreatureReady(player1, new MaskedGorgon());
        addCreatureReady(player2, new AnuridBrushhopper());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, Map.of(0, 0));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Anurid Brushhopper");
        harness.assertOnBattlefield(player1, "Masked Gorgon");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Threshold prevents green and white creatures from blocking Masked Gorgon")
    void thresholdPreventsGreenAndWhiteBlockers() {
        Permanent attacker = addCreatureReady(player1, new MaskedGorgon());
        Permanent greenBlocker = addCreatureReady(player2, new IronshellBeetle());
        Permanent whiteBlocker = addCreatureReady(player2, new SuntailHawk());
        List<Permanent> defenders = gd.playerBattlefields.get(player2.getId());

        assertThat(bls.canBlockAttacker(gd, greenBlocker, attacker, defenders)).isTrue();
        assertThat(bls.canBlockAttacker(gd, whiteBlocker, attacker, defenders)).isTrue();
        fillGraveyard(player1, 7);
        assertThat(bls.canBlockAttacker(gd, greenBlocker, attacker, defenders)).isFalse();
        assertThat(bls.canBlockAttacker(gd, whiteBlocker, attacker, defenders)).isFalse();
        fillGraveyard(player1, 6);
        assertThat(bls.canBlockAttacker(gd, greenBlocker, attacker, defenders)).isTrue();
        assertThat(bls.canBlockAttacker(gd, whiteBlocker, attacker, defenders)).isTrue();
    }
}
