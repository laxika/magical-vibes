package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RevokePrivileges;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StubbornBurrowfiend.class, Forest.class, GrizzlyBears.class, RevokePrivileges.class})
class StubbornBurrowfiendTest extends BaseCardTest {

    @Test
    @DisplayName("Saddling mills two cards, then boosts by creature cards in the graveyard")
    void saddlingMillsAndBoostsByCreatureCardsInGraveyard() {
        Permanent burrowfiend = addCreatureReady(player1, new StubbornBurrowfiend());
        Permanent saddler = addCreatureReady(player1, new GrizzlyBears());
        Card creatureAlreadyInGraveyard = new GrizzlyBears();
        Card milledCreature = new GrizzlyBears();
        Card milledLand = new Forest();
        harness.setGraveyard(player1, List.of(creatureAlreadyInGraveyard));
        harness.setLibrary(player1, List.of(milledCreature, milledLand));

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(burrowfiend.isSaddled()).isTrue();
        assertThat(saddler.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, burrowfiend)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, burrowfiend)).isEqualTo(4);
    }

    @Test
    @DisplayName("The saddle trigger fires only once each turn")
    void saddleTriggerFiresOnlyOnceEachTurn() {
        Permanent burrowfiend = addCreatureReady(player1, new StubbornBurrowfiend());
        Permanent firstSaddler = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstSaddler.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, burrowfiend)).isEqualTo(4);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, burrowfiend)).isEqualTo(4);
    }

    @Test
    void shortLibraryStillBoostsIgnoringOpponentsGraveyard() {
        Permanent burrowfiend = addCreatureReady(player1, new StubbornBurrowfiend());
        addCreatureReady(player1, new StubbornBurrowfiend());
        Card milledCreature = new StubbornBurrowfiend();
        harness.setLibrary(player1, List.of(milledCreature));
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new StubbornBurrowfiend()));

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCreature);
        assertThat(gqs.getEffectivePower(gd, burrowfiend)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, burrowfiend)).isEqualTo(3);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, burrowfiend)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, burrowfiend)).isEqualTo(3);
    }

    @Test
    void emptyLibraryStillBoostsByExistingCreatureCards() {
        Permanent burrowfiend = addCreatureReady(player1, new StubbornBurrowfiend());
        addCreatureReady(player1, new StubbornBurrowfiend());
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new StubbornBurrowfiend()));

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(burrowfiend.isSaddled()).isTrue();
        assertThat(gqs.getEffectivePower(gd, burrowfiend)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, burrowfiend)).isEqualTo(3);
    }

    @Test
    void creatureForbiddenFromCrewingCanStillSaddle() {
        Permanent burrowfiend = addCreatureReady(player1, new StubbornBurrowfiend());
        Permanent saddler = addCreatureReady(player1, new StubbornBurrowfiend());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new RevokePrivileges());
        aura.setAttachedTo(saddler.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(saddler.isTapped()).isTrue();
        assertThat(burrowfiend.isSaddled()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
