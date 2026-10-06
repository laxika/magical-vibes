package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GibberingHyenas;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReignOfTerror.class, GibberingHyenas.class, IronTuskElephant.class, RestInPeace.class})
class ReignOfTerrorTest extends BaseCardTest {

    private void castReign(int mode) {
        harness.setHand(player1, List.of(new ReignOfTerror()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);
        harness.castAndResolveSorcery(player1, 0, mode);
    }

    @Test
    @DisplayName("Green mode destroys only green creatures and costs 2 life each")
    void greenMode() {
        harness.addToBattlefield(player1, new GibberingHyenas());
        harness.addToBattlefield(player2, new GibberingHyenas());
        harness.addToBattlefield(player2, new IronTuskElephant());

        castReign(0);

        harness.assertNotOnBattlefield(player1, "Gibbering Hyenas");
        harness.assertNotOnBattlefield(player2, "Gibbering Hyenas");
        harness.assertOnBattlefield(player2, "Iron Tusk Elephant");

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("White mode destroys only white creatures and costs 2 life each")
    void whiteMode() {
        harness.addToBattlefield(player1, new IronTuskElephant());
        harness.addToBattlefield(player2, new IronTuskElephant());
        harness.addToBattlefield(player2, new GibberingHyenas());

        castReign(1);

        harness.assertNotOnBattlefield(player1, "Iron Tusk Elephant");
        harness.assertNotOnBattlefield(player2, "Iron Tusk Elephant");
        harness.assertOnBattlefield(player2, "Gibbering Hyenas");

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No matching creatures means no life loss")
    void noCreaturesNoLifeLoss() {
        harness.addToBattlefield(player2, new IronTuskElephant());

        castReign(0);

        harness.assertOnBattlefield(player2, "Iron Tusk Elephant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Green creatures can't be regenerated after the green mode destroys them")
    void cannotBeRegenerated() {
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player2, new GibberingHyenas());
        greenCreature.setRegenerationShield(1);

        castReign(0);

        harness.assertNotOnBattlefield(player2, "Gibbering Hyenas");
        harness.assertInGraveyard(player2, "Gibbering Hyenas");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("White creatures cannot regenerate either")
    void whiteModeCannotBeRegenerated() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IronTuskElephant());
        creature.setRegenerationShield(1);

        castReign(1);

        harness.assertNotOnBattlefield(player2, "Iron Tusk Elephant");
        harness.assertInGraveyard(player2, "Iron Tusk Elephant");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Indestructible creatures survive and do not contribute to life loss")
    void indestructibleCreatureDoesNotCount() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GibberingHyenas());
        survivor.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addToBattlefield(player2, new GibberingHyenas());

        castReign(0);

        harness.assertOnBattlefield(player1, "Gibbering Hyenas");
        harness.assertNotInGraveyard(player1, "Gibbering Hyenas");
        harness.assertInGraveyard(player2, "Gibbering Hyenas");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A shield counter can prevent destruction despite the regeneration restriction")
    void shieldCounterPreventsDestruction() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new GibberingHyenas());
        survivor.setCounterCount(CounterType.SHIELD, 1);

        castReign(0);

        harness.assertOnBattlefield(player2, "Gibbering Hyenas");
        assertThat(survivor.getCounterCount(CounterType.SHIELD)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Creatures exiled instead of dying do not cause life loss")
    @CardUsed({RestInPeace.class})
    void exiledCreaturesDoNotCountAsDeaths() {
        harness.addToBattlefield(player1, new RestInPeace());
        GibberingHyenas ownCreature = new GibberingHyenas();
        GibberingHyenas opposingCreature = new GibberingHyenas();
        harness.addToBattlefield(player1, ownCreature);
        harness.addToBattlefield(player2, opposingCreature);

        castReign(0);

        harness.assertNotOnBattlefield(player1, "Gibbering Hyenas");
        harness.assertNotOnBattlefield(player2, "Gibbering Hyenas");
        harness.assertNotInGraveyard(player1, "Gibbering Hyenas");
        harness.assertNotInGraveyard(player2, "Gibbering Hyenas");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownCreature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opposingCreature);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
