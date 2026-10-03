package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Biotransference.class, GiantGrowth.class, GrizzlyBears.class, SolRing.class})
class BiotransferenceTest extends BaseCardTest {

    @Test
    void creatureSpellTriggersAndCreatesAnArtifactCreatureToken() {
        harness.addToBattlefield(player1, new Biotransference());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.isArtifact(gd, tokens.getFirst())).isTrue();
        assertThat(gqs.isCreature(gd, tokens.getFirst())).isTrue();
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(2);
    }

    @Test
    void artifactSpellTriggers() {
        harness.addToBattlefield(player1, new Biotransference());
        harness.setHand(player1, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && gqs.getEffectiveCardTypes(gd, permanent).contains(CardType.ARTIFACT));
    }

    @Test
    void nonartifactSpellDoesNotTrigger() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Biotransference());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void battlefieldGrantUsesControlRatherThanOwnership() {
        harness.addToBattlefield(player1, new Biotransference());
        GrizzlyBears ownedCreature = new GrizzlyBears();
        ownedCreature.setOwnerId(player1.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, ownedCreature);
        GrizzlyBears borrowedCreature = new GrizzlyBears();
        borrowedCreature.setOwnerId(player2.getId());
        Permanent borrowed = harness.addToBattlefieldAndReturn(player1, borrowedCreature);

        assertThat(gqs.isArtifact(gd, stolen)).isFalse();
        assertThat(gqs.isArtifact(gd, borrowed)).isTrue();
        assertThat(gqs.isCreature(gd, borrowed)).isTrue();
    }

    @Test
    void creatureCardsInEachNonbattlefieldZoneAreArtifactsWhileSourceRemains() {
        harness.addToBattlefield(player1, new Biotransference());
        GrizzlyBears hand = new GrizzlyBears();
        GrizzlyBears library = new GrizzlyBears();
        GrizzlyBears graveyard = new GrizzlyBears();
        GrizzlyBears exile = new GrizzlyBears();
        harness.setHand(player1, List.of(hand));
        harness.setLibrary(player1, List.of(library));
        harness.setGraveyard(player1, List.of(graveyard));
        harness.setExile(player1, List.of(exile));

        for (GrizzlyBears card : List.of(hand, library, graveyard, exile)) {
            card.setOwnerId(player1.getId());
            assertThat(gqs.cardHasType(card, CardType.ARTIFACT, gd, player1.getId())).isTrue();
            assertThat(gqs.cardHasType(card, CardType.CREATURE, gd, player1.getId())).isTrue();
        }

        gd.playerBattlefields.get(player1.getId()).clear();

        for (GrizzlyBears card : List.of(hand, library, graveyard, exile)) {
            assertThat(gqs.cardHasType(card, CardType.ARTIFACT, gd, player1.getId())).isFalse();
        }
    }

    @Test
    void opponentArtifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Biotransference());
        harness.setHand(player2, List.of(new SolRing()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void twoCopiesTriggerSeparatelyForOneCreatureSpell() {
        harness.addToBattlefield(player1, new Biotransference());
        harness.addToBattlefield(player1, new Biotransference());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(2);
    }

    @Test
    void pendingTriggerStillCreatesArtifactTokenAfterSourceLeaves() {
        harness.addToBattlefield(player1, new Biotransference());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.isArtifact(gd, tokens.getFirst())).isTrue();
        assertThat(gqs.isCreature(gd, tokens.getFirst())).isTrue();
        assertThat(gqs.getEffectiveColors(gd, tokens.getFirst())).containsExactly(CardColor.BLACK);
        assertThat(tokens.getFirst().getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.NECRON, CardSubtype.WARRIOR);
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(2);
    }
}
