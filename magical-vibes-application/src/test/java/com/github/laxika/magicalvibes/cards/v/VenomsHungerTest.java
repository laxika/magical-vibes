package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DocOcksHenchmen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenomsHunger.class, DocOcksHenchmen.class, GrizzlyBears.class, Plains.class})
class VenomsHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {2} less while controlling a Villain")
    void costsLessWhileControllingVillain() {
        harness.addToBattlefield(player1, new DocOcksHenchmen());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VenomsHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Requires its full cost without a Villain")
    void requiresFullCostWithoutVillain() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VenomsHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys the target creature and gives the spell's controller 2 life")
    void destroysCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new VenomsHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new VenomsHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID plainsId = harness.getPermanentId(player2, "Plains");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, plainsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Villain does not reduce the cost")
    void opponentVillainDoesNotReduceCost() {
        harness.addToBattlefield(player2, new DocOcksHenchmen());
        harness.setHand(player1, List.of(new VenomsHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Doc Ock's Henchmen");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Villains reduce the cost by only two")
    void multipleVillainsDoNotStackDiscount() {
        harness.addToBattlefield(player1, new DocOcksHenchmen());
        harness.addToBattlefield(player1, new DocOcksHenchmen());
        harness.addToBattlefield(player2, new DocOcksHenchmen());
        harness.setHand(player1, List.of(new VenomsHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0,
                harness.getPermanentId(player2, "Doc Ock's Henchmen"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Doc Ock's Henchmen");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can destroy your own Villain using its discount")
    void canDestroyOwnVillain() {
        harness.addToBattlefield(player1, new DocOcksHenchmen());
        harness.setHand(player1, List.of(new VenomsHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0,
                harness.getPermanentId(player1, "Doc Ock's Henchmen"));
        harness.assertNotOnBattlefield(player1, "Doc Ock's Henchmen");
        harness.assertInGraveyard(player1, "Doc Ock's Henchmen");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("No life gain when the only target leaves before resolution")
    void noLifeGainWhenTargetLeaves() {
        harness.addToBattlefield(player2, new DocOcksHenchmen());
        harness.setHand(player1, List.of(new VenomsHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID targetId = harness.getPermanentId(player2, "Doc Ock's Henchmen");
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Venom's Hunger");
    }
}
