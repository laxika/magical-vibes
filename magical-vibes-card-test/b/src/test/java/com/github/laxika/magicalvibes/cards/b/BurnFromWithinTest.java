package com.github.laxika.magicalvibes.cards.b;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PalisadeGiant;
import com.github.laxika.magicalvibes.cards.w.WithstandDeath;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({BurnFromWithin.class, GrizzlyBears.class, WithstandDeath.class})
class BurnFromWithinTest extends BaseCardTest {

    @Test
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new BurnFromWithin()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void killedCreatureIsExiledInsteadOfDying() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurnFromWithin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void removesIndestructibleBeforeLethalStateBasedActions() {
        Permanent bears = indestructibleBears();
        harness.setHand(player1, List.of(new BurnFromWithin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void zeroDamageDoesNotRemoveIndestructible() {
        Permanent bears = indestructibleBears();
        harness.setHand(player1, List.of(new BurnFromWithin()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0, bears.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void nonlethalDamageRemovesIndestructible() {
        Permanent bears = indestructibleBears();
        harness.setHand(player1, List.of(new BurnFromWithin()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 1, bears.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void laterIndestructibleGrantAppliesAfterRemoval() {
        Permanent bears = indestructibleBears();
        harness.setHand(player1, List.of(new BurnFromWithin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 1, bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.setHand(player2, List.of(new WithstandDeath()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @CardUsed({BurnFromWithin.class, GrizzlyBears.class, WithstandDeath.class, PalisadeGiant.class})
    void redirectedDamageRemovesIndestructibleFromActualRecipient() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new PalisadeGiant());
        harness.setHand(player2, List.of(new WithstandDeath()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, giant.getId());
        harness.setHand(player1, List.of(new BurnFromWithin()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castAndResolveSorcery(player1, 0, 7, bears.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Palisade Giant");
        harness.assertNotInGraveyard(player2, "Palisade Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Palisade Giant"));
    }

    @Test
    @CardUsed({BurnFromWithin.class, GrizzlyBears.class, PalisadeGiant.class})
    void redirectedLethalDamageExilesActualRecipientWithoutIndestructible() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new PalisadeGiant());
        harness.setHand(player1, List.of(new BurnFromWithin()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castAndResolveSorcery(player1, 0, 7, bears.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Palisade Giant");
        harness.assertNotInGraveyard(player2, "Palisade Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Palisade Giant"));
    }

    private Permanent indestructibleBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new WithstandDeath()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        return bears;
    }
}
