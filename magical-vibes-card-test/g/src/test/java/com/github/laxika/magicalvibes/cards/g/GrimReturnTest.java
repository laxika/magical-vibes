package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.s.ScavengingOoze;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimReturn.class, DoomBlade.class, ElvishMystic.class, Fog.class,
        ScavengingOoze.class, TomeScour.class})
class GrimReturnTest extends BaseCardTest {

    @Test
    @DisplayName("Puts an opponent's creature that died this turn onto the battlefield under your control")
    void reanimatesOpponentCreatureThatDiedThisTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ElvishMystic());

        harness.setHand(player1, List.of(new DoomBlade(), new GrimReturn()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.castAndResolveInstant(player1, 0, bears.getCard().getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(bears.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(bears.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a creature card that was not put into the graveyard this turn")
    void cannotTargetCardAlreadyInGraveyard() {
        Card creature = new ElvishMystic();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new GrimReturn()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("from the battlefield this turn");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Card instant = new Fog();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new GrimReturn()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target creature card leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ElvishMystic());

        harness.setHand(player1, List.of(new DoomBlade(), new GrimReturn()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.castInstant(player1, 0, bears.getCard().getId());
        GameData gd = harness.getGameData();
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(bears.getCard().getId()));
    }
    @Test
    @DisplayName("Returns your own creature untapped with summoning sickness")
    void reanimatesOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishMystic());
        creature.tap();
        harness.setHand(player1, List.of(new DoomBlade(), new GrimReturn()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getCard().getId());

        Permanent returned = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creature.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
        harness.assertNotInGraveyard(player1, "Elvish Mystic");
    }

    @Test
    @DisplayName("Cannot target a creature milled this turn")
    void cannotTargetCreatureMilledThisTurn() {
        Card creature = new ElvishMystic();
        harness.setLibrary(player2, List.of(creature));
        harness.setHand(player1, List.of(new TomeScour(), new GrimReturn()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.assertInGraveyard(player2, "Elvish Mystic");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("from the battlefield this turn");
    }

    @Test
    @DisplayName("Cannot target a creature that died on the previous turn")
    void cannotTargetCreatureThatDiedLastTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ElvishMystic());
        harness.setHand(player1, List.of(new DoomBlade(), new GrimReturn()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("from the battlefield this turn");
    }

    @Test
    @DisplayName("Does not reanimate a target exiled in response")
    void targetExiledInResponse() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ElvishMystic());
        harness.addToBattlefield(player2, new ScavengingOoze());
        harness.setHand(player1, List.of(new DoomBlade(), new GrimReturn()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.castInstant(player1, 0, creature.getCard().getId());
        harness.activateAbility(player2, 0, 0, null, creature.getCard().getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elvish Mystic");
        harness.assertNotInGraveyard(player2, "Elvish Mystic");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(creature.getCard().getId()));
        harness.assertInGraveyard(player1, "Grim Return");
    }
}
