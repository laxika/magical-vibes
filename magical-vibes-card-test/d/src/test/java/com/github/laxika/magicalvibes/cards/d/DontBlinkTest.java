package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArcaneSignet;
import com.github.laxika.magicalvibes.cards.c.CybermanPatrol;
import com.github.laxika.magicalvibes.cards.e.EternalScourge;
import com.github.laxika.magicalvibes.cards.p.PsychicSurgery;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DontBlink.class, EternalScourge.class, CybermanPatrol.class,
        ArcaneSignet.class, Displace.class, PsychicSurgery.class})
class DontBlinkTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles a creature entering from exile into its owner's library")
    void shufflesCreatureEnteringFromExile() {
        castDontBlink();

        CybermanPatrol creature = new CybermanPatrol();
        harness.setLibrary(player2, List.of());
        harness.setExile(player2, List.of(creature));
        gd.removeFromExile(creature.getId());
        Permanent entering = new Permanent(creature, Zone.EXILE);
        harness.inMutationScope(() -> harness.getBattlefieldEntryService()
                .putPermanentOntoBattlefield(gd, player2.getId(), entering));

        harness.assertNotOnBattlefield(player2, "Cyberman Patrol");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Shuffles a creature cast from exile into its owner's library")
    void shufflesCreatureCastFromExile() {
        castDontBlink();

        EternalScourge scourge = new EternalScourge();
        harness.setLibrary(player2, List.of());
        harness.setExile(player2, List.of(scourge));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castFromExile(player2, scourge.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Eternal Scourge");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(scourge);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(scourge);
    }

    @Test
    @DisplayName("Cycling draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DontBlink()));
        harness.setLibrary(player1, List.of(new CybermanPatrol()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Don't Blink");
        harness.assertInHand(player1, "Cyberman Patrol");
    }

    @Test
    void doesNotReplaceCreatureCastFromHand() {
        castDontBlink();

        harness.castFromHand(player1, new CybermanPatrol(), "{2}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cyberman Patrol");
    }

    @Test
    void doesNotReplaceNoncreatureEnteringFromExile() {
        castDontBlink();

        ArcaneSignet signet = new ArcaneSignet();
        harness.setExile(player2, List.of(signet));
        gd.removeFromExile(signet.getId());
        Permanent entering = new Permanent(signet, Zone.EXILE);
        harness.inMutationScope(() -> harness.getBattlefieldEntryService()
                .putPermanentOntoBattlefield(gd, player2.getId(), entering));

        harness.assertOnBattlefield(player2, "Arcane Signet");
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(signet);
    }

    @Test
    void shufflesIntoOwnersLibraryRatherThanControllersLibrary() {
        castDontBlink();

        CybermanPatrol creature = new CybermanPatrol();
        creature.setOwnerId(player1.getId());
        harness.setLibrary(player1, List.of());
        harness.setExile(player1, List.of(creature));
        gd.removeFromExile(creature.getId());
        Permanent entering = new Permanent(creature, Zone.EXILE);
        harness.inMutationScope(() -> harness.getBattlefieldEntryService()
                .putPermanentOntoBattlefield(gd, player2.getId(), entering));

        harness.assertNotOnBattlefield(player2, "Cyberman Patrol");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    void replacementExpiresAtEndOfTurn() {
        castDontBlink();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        EternalScourge scourge = new EternalScourge();
        harness.setExile(player2, List.of(scourge));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castFromExile(player2, scourge.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Eternal Scourge");
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(scourge);
    }

    @Test
    void simultaneousReturnShufflesOwnersLibraryOnlyOnce() {
        castDontBlink();
        harness.addToBattlefield(player1, new PsychicSurgery());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new EternalScourge());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new EternalScourge());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new Displace()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, List.of(first.getId(), second.getId()));

        harness.assertNotOnBattlefield(player2, "Eternal Scourge");
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyInAnyOrder(first.getOriginalCard(), second.getOriginalCard());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId())
                .isEqualTo(harness.getPermanentId(player1, "Psychic Surgery"));
    }

    @Test
    void replacesFaceDownCreatureEvenWhenPrintedCardIsNotCreature() {
        castDontBlink();

        ArcaneSignet signet = new ArcaneSignet();
        harness.setLibrary(player2, List.of());
        harness.setExile(player2, List.of(signet));
        gd.removeFromExile(signet.getId());
        Permanent entering = new Permanent(signet, Zone.EXILE);
        entering.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.inMutationScope(() -> harness.getBattlefieldEntryService()
                .putPermanentOntoBattlefield(gd, player2.getId(), entering));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(signet);
    }

    private void castDontBlink() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new DontBlink(), "{1}{U}");
        harness.passBothPriorities();
    }
}
