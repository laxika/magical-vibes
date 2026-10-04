package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BoggartBirthRite;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ElvishEulogist.class, ElvishPromenade.class, WoodlandChangeling.class, BoggartBirthRite.class})
class ElvishEulogistTest extends BaseCardTest {

    private static Card elfCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.ELF));
        return card;
    }

    private static Card nonElfCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.BEAR));
        return card;
    }

    @Test
    @DisplayName("Sacrificing gains 1 life per Elf card in graveyard, counting itself")
    void gainsLifePerElfInGraveyard() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player1, List.of(elfCard("Wren's Run Vanquisher"), elfCard("Imperious Perfect"), nonElfCard("Grizzly Bears")));

        int lifeBefore = harness.getGameData().getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // 2 Elf cards already in the graveyard + the sacrificed Eulogist itself = 3 life.
        harness.assertLife(player1, lifeBefore + 3);
        harness.assertNotOnBattlefield(player1, "Elvish Eulogist");
        harness.assertInGraveyard(player1, "Elvish Eulogist");
    }

    @Test
    @DisplayName("With no other Elf cards, sacrificing still gains 1 life from itself")
    void gainsOneLifeFromItselfWhenGraveyardEmpty() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player1, List.of(nonElfCard("Grizzly Bears")));

        int lifeBefore = harness.getGameData().getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("Elf kindred spells and changelings both count as Elf cards")
    void countsNoncreatureElvesAndChangelings() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player1, List.of(new ElvishPromenade(), new WoodlandChangeling(), new BoggartBirthRite()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("A tapped Eulogist can activate and ignores opponents' Elf cards")
    void ignoresOpponentsGraveyardAndNeedsNoTap() {
        harness.addToBattlefieldAndReturn(player1, new ElvishEulogist()).setTapped(true);
        harness.setGraveyard(player2, List.of(new ElvishEulogist(), new ElvishPromenade(), new WoodlandChangeling()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Elvish Eulogist");
        harness.assertInGraveyard(player1, "Elvish Eulogist");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Elf cards added before resolution increase the life gained")
    void countsGraveyardAtResolution() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        gd.playerGraveyards.get(player1.getId()).add(new ElvishPromenade());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Removing all Elf cards before resolution results in zero life gained")
    void gainsNoLifeWhenGraveyardEmptiedBeforeResolution() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        Card sacrificedEulogist = gd.playerGraveyards.get(player1.getId()).getFirst();
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(sacrificedEulogist));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Elvish Eulogist");
    }
}
