package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerraParagon.class, Forest.class, GrizzlyBears.class, CrawWurm.class,
        LightningBolt.class, Murder.class, StoneRain.class})
class SerraParagonTest extends BaseCardTest {

    @Test
    @DisplayName("Shares one once-per-turn use between playing a land and casting a permanent")
    void sharesLandAndPermanentPermission() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A permanent cast from the graveyard gains the exile-and-life death ability")
    void grantsDeathAbilityToCastPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(bears);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        prepareMainPhase();
        harness.castInstant(player1, 0, returned.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("A land played from the graveyard also gains the death ability")
    void grantsDeathAbilityToPlayedLand() {
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(forest));
        harness.setHand(player1, List.of());
        prepareMainPhase();

        harness.playGraveyardLand(player1, 0);

        Permanent returned = findPermanent(forest);
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);
        prepareMainPhase();
        harness.castSorcery(player1, 0, returned.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .contains("Forest");
    }

    @Test
    @DisplayName("Only permanent cards with mana value 3 or less can be cast")
    void rejectsIneligibleCards() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setHand(player1, List.of());
        prepareMainPhase();

        harness.setGraveyard(player1, List.of(new Forest()));
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setGraveyard(player1, List.of(new LightningBolt()));
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setGraveyard(player1, List.of(new CrawWurm()));
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
