package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JaheiraHarperEmissary.class, Forest.class, GrizzlyBears.class, Island.class,
        MindStone.class, Mountain.class, Plains.class, Swamp.class})
class JaheiraHarperEmissaryTest extends BaseCardTest {

    @Test
    void whiteFaceDestroysAnArtifactAndCountersOtherCreatures() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        activateSpecialization(jaheira, 0, new Plains());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.handlePermanentChosen(player1, firstCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mind Stone");
        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void blueFaceScriesTwoWhenThereIsNoArtifactOrEnchantment() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island(), new Forest()));

        activateSpecialization(jaheira, 1, new Island());
        skipOptionalArtifactTarget();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void blackFaceMakesEachOpponentLoseThreeLife() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());

        activateSpecialization(jaheira, 2, new Swamp());
        skipOptionalArtifactTarget();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void greenFaceGainsFourLife() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());
        gd.playerLifeTotals.put(player1.getId(), 10);

        activateSpecialization(jaheira, 4, new Forest());
        skipOptionalArtifactTarget();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    void redFaceGivesTheNextCreatureSpellAPerpetualPowerBoostAndHaste() {
        Permanent jaheira = addCreatureReady(player1, new JaheiraHarperEmissary());

        activateSpecialization(jaheira, 3, new Mountain());
        skipOptionalArtifactTarget();
        harness.passBothPriorities();

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bearPermanent = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bearPermanent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bearPermanent)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bearPermanent, Keyword.HASTE)).isTrue();
    }

    private void activateSpecialization(Permanent jaheira, int abilityIndex,
                                        com.github.laxika.magicalvibes.model.Card discarded) {
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }

    private void skipOptionalArtifactTarget() {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (choice != null) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
    }

}
