package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirrorBox;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FolkHero.class, GrizzlyBears.class, Forest.class, Shock.class, NamelessInversion.class, MirrorBox.class})
class FolkHeroTest extends BaseCardTest {

    @Test
    void commanderDrawsWhenYouCastASharedCreatureTypeSpell() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        harness.addToBattlefield(player1, new FolkHero());

        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void ordinaryCreatureDoesNotGainTheAbility() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FolkHero());

        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void doesNotTriggerForASpellWithoutCreatureTypes() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        harness.addToBattlefield(player1, new FolkHero());

        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void triggersOnlyOnceEachTurnForEachCommander() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        harness.addToBattlefield(player1, new FolkHero());

        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondDraw);
    }

    @Test
    void kindredSpellWithChangelingTriggersTheDraw() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        harness.addToBattlefield(player1, new FolkHero());
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void ownedCommanderControlledByOpponentGrantsDrawToItsController() {
        Card commander = new GrizzlyBears();
        commander.setOwnerId(player1.getId());
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player2, commander);
        harness.addToBattlefield(player1, new FolkHero());
        Forest drawn = new Forest();
        harness.setLibrary(player2, List.of(drawn));

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentOwnedCommanderDoesNotGainAbilityWhenYouControlIt() {
        Card commander = new GrizzlyBears();
        commander.setOwnerId(player2.getId());
        gd.makeCommander(player2.getId(), commander);
        addCreatureReady(player1, commander);
        harness.addToBattlefield(player1, new FolkHero());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void nonmatchingCastDoesNotUseTheOncePerTurnTrigger() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        harness.addToBattlefield(player1, new FolkHero());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void eachFolkHeroGrantsAnIndependentOncePerTurnAbility() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        harness.addToBattlefield(player1, new MirrorBox());
        harness.addToBattlefield(player1, new FolkHero());
        harness.addToBattlefield(player1, new FolkHero());
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        Forest thirdDraw = new Forest();
        GrizzlyBears secondSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.setHand(player1, List.of(new GrizzlyBears(), secondSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(secondSpell, firstDraw, secondDraw);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(thirdDraw);
    }
}
