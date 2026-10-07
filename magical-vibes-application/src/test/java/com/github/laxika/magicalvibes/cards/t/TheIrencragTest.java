package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BarrowNaughty;
import com.github.laxika.magicalvibes.cards.r.RubyDaringTracker;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheIrencrag.class, BarrowNaughty.class, RubyDaringTracker.class, ToughCookie.class})
class TheIrencragTest extends BaseCardTest {

    @Test
    void tappingAddsColorlessMana() {
        Permanent irencrag = addIrencragReady();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(irencrag.isTapped()).isTrue();
    }

    @Test
    void legendaryCreatureOffersTransformation() {
        Permanent irencrag = addIrencragReady();

        offerTransformation();

        assertThat(gqs.getEffectiveName(gd, irencrag)).isEqualTo("Everflame, Heroes' Legacy");
        assertThat(gqs.permanentHasSubtype(irencrag, CardSubtype.EQUIPMENT)).isTrue();
    }

    @Test
    void transformedIrencragCanEquipAndBoostCreature() {
        Permanent irencrag = addIrencragReady();
        offerTransformation();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BarrowNaughty());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(irencrag.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    void nonlegendaryCreatureDoesNotOfferTransformation() {
        Permanent irencrag = addIrencragReady();

        harness.enterBattlefieldAndReturn(player1, new BarrowNaughty());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveName(gd, irencrag)).isEqualTo("The Irencrag");
    }

    @Test
    void decliningTransformationLeavesIrencragUnchanged() {
        Permanent irencrag = addIrencragReady();

        offerTransformation(false);

        assertThat(gqs.getEffectiveName(gd, irencrag)).isEqualTo("The Irencrag");
        assertThat(gqs.permanentHasSubtype(irencrag, CardSubtype.EQUIPMENT)).isFalse();
    }

    @Test
    void transformedIrencragDoesNotTriggerAgain() {
        addIrencragReady();
        offerTransformation();

        harness.enterBattlefieldAndReturn(player1, new RubyDaringTracker());

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addIrencragReady() {
        return harness.addToBattlefieldAndReturn(player1, new TheIrencrag());
    }

    private void offerTransformation() {
        offerTransformation(true);
    }

    private void offerTransformation(boolean accept) {
        harness.enterBattlefieldAndReturn(player1, new RubyDaringTracker());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accept);
    }

    @Test
    void opposingLegendaryCreatureDoesNotOfferTransformation() {
        Permanent irencrag = addIrencragReady();

        harness.enterBattlefieldAndReturn(player2, new RubyDaringTracker());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveName(gd, irencrag)).isEqualTo("The Irencrag");
    }

    @Test
    void decliningTransformationPreservesManaAbility() {
        Permanent irencrag = addIrencragReady();
        offerTransformation(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(irencrag.isTapped()).isTrue();
    }

    @Test
    void transformationReplacesEarlierCreatureAnimation() {
        Permanent irencrag = addIrencragReady();
        harness.addToBattlefield(player1, new ToughCookie());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 1, 0, irencrag.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, irencrag)).isTrue();

        offerTransformation();

        assertThat(gqs.getEffectiveName(gd, irencrag)).isEqualTo("Everflame, Heroes' Legacy");
        assertThat(gqs.isCreature(gd, irencrag)).isFalse();
    }
}
