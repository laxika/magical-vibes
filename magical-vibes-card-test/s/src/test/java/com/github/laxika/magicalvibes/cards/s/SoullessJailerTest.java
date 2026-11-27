package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BeanstalkGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.MuseVortex;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.o.ObzedatsAid;
import com.github.laxika.magicalvibes.cards.r.RiseFromTheGrave;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoullessJailer.class, GrizzlyBears.class, IcyManipulator.class,
        ObzedatsAid.class, RiseFromTheGrave.class, Shock.class, ThinkTwice.class, TurnToFrog.class,
        MuseVortex.class, Ponder.class, BeanstalkGiant.class, Forest.class})
class SoullessJailerTest extends BaseCardTest {

    @Test
    @DisplayName("Permanent cards cannot enter the battlefield from graveyards")
    void blocksPermanentCardsFromGraveyards() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addToBattlefield(player1, new SoullessJailer());
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Noncreature permanent cards cannot enter the battlefield from graveyards")
    void blocksNoncreaturePermanentCardsFromGraveyards() {
        IcyManipulator artifact = new IcyManipulator();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addToBattlefield(player1, new SoullessJailer());
        harness.setHand(player1, List.of(new ObzedatsAid()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Icy Manipulator");
        harness.assertInGraveyard(player1, "Icy Manipulator");
    }

    @Test
    @DisplayName("Noncreature spells cannot be cast from graveyards")
    void blocksNoncreatureSpellsFromGraveyards() {
        harness.addToBattlefield(player1, new SoullessJailer());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature spells cannot be cast from exile")
    void blocksNoncreatureSpellsFromExile() {
        ThinkTwice thinkTwice = new ThinkTwice();
        harness.addToBattlefield(player1, new SoullessJailer());
        harness.setExile(player1, List.of(thinkTwice));
        gd.grantExilePlayPermission(thinkTwice.getId(), player1.getId(), Duration.PERMANENT);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, thinkTwice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature spells can still be cast from hand")
    void allowsNoncreatureSpellsFromHand() {
        harness.addToBattlefield(player1, new SoullessJailer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Creature spells can still be cast from exile")
    void allowsCreatureSpellsFromExile() {
        SoullessJailer exiledCreature = new SoullessJailer();
        harness.addToBattlefield(player1, new SoullessJailer());
        harness.setExile(player1, List.of(exiledCreature));
        gd.grantExilePlayPermission(exiledCreature.getId(), player1.getId(), Duration.PERMANENT);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, exiledCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(exiledCreature.getId()));
    }

    @Test
    @DisplayName("An opponent's flashback spell can't be cast from their graveyard")
    void blocksOpponentFlashback() {
        harness.addToBattlefield(player1, new SoullessJailer());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castFlashback(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Permanent cards can enter from graveyards once Soulless Jailer loses its abilities")
    void allowsGraveyardReturnAfterLosingAbilities() {
        harness.addToBattlefield(player1, new SoullessJailer());
        IcyManipulator artifact = new IcyManipulator();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new TurnToFrog(), new ObzedatsAid()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Soulless Jailer"));
        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        harness.assertOnBattlefield(player1, "Icy Manipulator");
        harness.assertNotInGraveyard(player1, "Icy Manipulator");
    }

    @Test
    @DisplayName("Flashback works once Soulless Jailer loses its abilities")
    void allowsFlashbackAfterLosingAbilities() {
        harness.addToBattlefield(player1, new SoullessJailer());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.setLibrary(player1, List.of(new SoullessJailer()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Soulless Jailer"));
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Soulless Jailer");
        harness.assertNotInGraveyard(player1, "Think Twice");
    }

    @Test
    @DisplayName("Noncreature spells can be cast from exile once Soulless Jailer loses its abilities")
    void allowsNoncreatureSpellFromExileAfterLosingAbilities() {
        harness.addToBattlefield(player1, new SoullessJailer());
        ThinkTwice spell = new ThinkTwice();
        harness.setExile(player1, List.of(spell));
        gd.grantExilePlayPermission(spell.getId(), player1.getId(), Duration.PERMANENT);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.setLibrary(player1, List.of(new SoullessJailer()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Soulless Jailer"));
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Soulless Jailer");
        harness.assertInGraveyard(player1, "Think Twice");
    }

    @Test
    @DisplayName("A noncreature spell an effect lets its controller cast from exile while resolving can't be cast")
    void blocksNoncreatureResolutionCastFromExile() {
        Ponder ponder = new Ponder();
        harness.addToBattlefield(player2, new SoullessJailer());
        castMuseVortex(1, List.of(ponder, new Forest()));

        if (gd.interaction.activeInteraction() != null) {
            harness.handleMultipleCardsChosen(player1, List.of(ponder.getId()));
        }

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == ponder);
        assertThat(gd.playerHands.get(player1.getId())).contains(ponder);
    }

    @Test
    @DisplayName("A creature card's Adventure cast from exile while an effect resolves is a noncreature spell it stops")
    void blocksAdventureResolutionCastFromExile() {
        BeanstalkGiant giant = new BeanstalkGiant();
        harness.addToBattlefield(player2, new SoullessJailer());
        castMuseVortex(3, List.of(giant, new Forest(), new Forest(), new Forest()));

        if (gd.interaction.activeInteraction() != null) {
            harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));
        }

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(giant.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(giant);
    }

    private void castMuseVortex(int xValue, List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new MuseVortex()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }
}
