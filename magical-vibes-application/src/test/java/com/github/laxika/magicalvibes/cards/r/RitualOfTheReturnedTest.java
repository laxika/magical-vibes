package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CharRumbler;
import com.github.laxika.magicalvibes.cards.c.ConsumingAberration;
import com.github.laxika.magicalvibes.cards.h.HeroesBane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RitualOfTheReturned.class, GrizzlyBears.class, Cancel.class,
        ConsumingAberration.class, CharRumbler.class, HeroesBane.class})
class RitualOfTheReturnedTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature from your graveyard and creates one stat-matched black Zombie")
    void exilesCreatureAndCreatesStatMatchedZombie() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new RitualOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).hasSize(1);
        List<Permanent> zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.ZOMBIE))
                .toList();
        assertThat(zombies).hasSize(1);
        assertThat(zombies.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(zombies.getFirst().getCard().getToughness()).isEqualTo(2);
        assertThat(zombies.getFirst().getCard().getColor()).isEqualTo(CardColor.BLACK);
    }

    @Test
    @DisplayName("Cannot target a noncreature card")
    void rejectsNonCreatureTarget() {
        Card cancel = new Cancel();
        harness.setGraveyard(player1, List.of(cancel));
        harness.setHand(player1, List.of(new RitualOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, cancel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates no token if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new RitualOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, bears.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void rejectsOpponentGraveyardTarget() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player1, List.of(new RitualOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses characteristic-defined graveyard stats and freezes the token's stats")
    void usesCharacteristicDefinedStats() {
        Card aberration = new ConsumingAberration();
        harness.setGraveyard(player1, List.of(aberration));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Cancel()));
        harness.setHand(player1, List.of(new RitualOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, aberration.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(aberration.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent zombie = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);

        harness.setGraveyard(player2, List.of());
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(zombie);
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Preserves negative power when setting the Zombie's stats")
    void preservesNegativePower() {
        Card rumbler = new CharRumbler();
        harness.setGraveyard(player1, List.of(rumbler));
        harness.setHand(player1, List.of(new RitualOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, rumbler.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(rumbler.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent zombie = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(zombie.getCard().getPower()).isEqualTo(-1);
        assertThat(zombie.getCard().getToughness()).isEqualTo(3);
        assertThat(zombie.getCard().getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("A zero-toughness Zombie dies without inheriting the exiled card's counters ability")
    void zeroToughnessTokenDies() {
        Card hydra = new HeroesBane();
        harness.setGraveyard(player1, List.of(hydra));
        harness.setHand(player1, List.of(new RitualOfTheReturned()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, hydra.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(hydra.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
