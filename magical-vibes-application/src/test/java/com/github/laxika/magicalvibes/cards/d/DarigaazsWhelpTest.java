package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FurnaceWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarigaazsWhelp.class, FurnaceWhelp.class, GrizzlyBears.class})
class DarigaazsWhelpTest extends BaseCardTest {

    @Test
    void drawingDragonPerpetuallyBoostsIt() {
        Permanent whelp = harness.addToBattlefieldAndReturn(player1, new DarigaazsWhelp());
        FurnaceWhelp dragon = new FurnaceWhelp();
        harness.setLibrary(player1, List.of(dragon));

        draw(player1.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.RED, 10);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent drawnDragon = findPermanent(player1, dragon);
        assertThat(gqs.getEffectivePower(gd, drawnDragon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drawnDragon)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(2);
    }

    @Test
    void kickedEntryBoostsTheWhelpAndSeeksAndBoostsADragon() {
        FurnaceWhelp dragon = new FurnaceWhelp();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), dragon));
        harness.setHand(player1, List.of(new DarigaazsWhelp()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent whelp = findPermanent(player1, DarigaazsWhelp.class);
        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, whelp)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);

        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.RED, 10);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent foundDragon = findPermanent(player1, dragon);
        assertThat(gqs.getEffectivePower(gd, foundDragon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, foundDragon)).isEqualTo(3);
    }

    @Test
    void drawingNonDragonDoesNotBoostIt() {
        harness.addToBattlefield(player1, new DarigaazsWhelp());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        draw(player1.getId());

        assertThat(gd.stack).isEmpty();
    }

    private void draw(UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }

    private Permanent findPermanent(Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }

    private Permanent findPermanent(Player player, Class<? extends Card> cardClass) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> cardClass.isInstance(permanent.getCard()))
                .findFirst()
                .orElseThrow();
    }
}
