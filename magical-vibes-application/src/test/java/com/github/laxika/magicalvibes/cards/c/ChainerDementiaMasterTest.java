package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Nightmare;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainerDementiaMaster.class, GrizzlyBears.class, Nightmare.class, Swamp.class, Coercion.class})
class ChainerDementiaMasterTest extends BaseCardTest {

    @Test
    @DisplayName("All Nightmares get +1/+1 regardless of controller")
    void buffsAllNightmares() {
        harness.addToBattlefield(player1, new ChainerDementiaMaster());
        harness.addToBattlefield(player2, new Swamp());
        Permanent nightmare = harness.addToBattlefieldAndReturn(player2, new Nightmare());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Pays life to return a creature from any graveyard as a black Nightmare")
    void returnsCreatureAsBlackNightmare() {
        harness.addToBattlefield(player1, new ChainerDementiaMaster());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gqs.getEffectiveColors(gd, returned)).contains(CardColor.GREEN, CardColor.BLACK);
        assertThat(GameQueryService.permanentHasSubtype(returned, CardSubtype.BEAR)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(returned, CardSubtype.NIGHTMARE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaving the battlefield exiles all Nightmares but not other creatures")
    void leavesBattlefieldExilesNightmares() {
        Permanent chainer = harness.addToBattlefieldAndReturn(player1, new ChainerDementiaMaster());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        Permanent nightmare = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, chainer));
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears).doesNotContain(nightmare);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        harness.addToBattlefield(player1, new ChainerDementiaMaster());
        Card noncreature = new Coercion();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
