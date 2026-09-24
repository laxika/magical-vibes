package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MischievousLookout.class, SolRing.class, Shatter.class, WalkingCorpse.class, Pacifism.class})
class MischievousLookoutTest extends BaseCardTest {

    @Test
    void castsEligiblePermanentAndPerpetuallyMakesItARatCreature() {
        harness.addToBattlefield(player1, new MischievousLookout());
        SolRing ring = new SolRing();
        harness.setGraveyard(player1, List.of(ring));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ringPermanent = findPermanent(player1, "Sol Ring");
        assertThat(gqs.isArtifact(gd, ringPermanent)).isTrue();
        assertThat(gqs.isCreature(gd, ringPermanent)).isTrue();
        assertThat(ringPermanent.getCard().getSubtypes()).contains(CardSubtype.RAT);
        assertThat(gqs.getEffectivePower(gd, ringPermanent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ringPermanent)).isEqualTo(1);

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, ringPermanent.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card ->
                card.getId().equals(ring.getId())
                        && card.hasType(CardType.ARTIFACT)
                        && card.hasType(CardType.CREATURE)
                        && card.getSubtypes().contains(CardSubtype.RAT)
                        && card.getPower() == 2
                        && card.getToughness() == 1);
    }

    @Test
    void rejectsCreatureAndAuraCards() {
        harness.addToBattlefield(player1, new MischievousLookout());
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new Pacifism()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
