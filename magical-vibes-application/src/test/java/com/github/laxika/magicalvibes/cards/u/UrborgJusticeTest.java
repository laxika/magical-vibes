package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.cards.z.ZulaportCutthroat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrborgJustice.class, BenalishKnight.class, MindStone.class, RedwoodTreefolk.class,
        ZulaportCutthroat.class})
class UrborgJusticeTest extends BaseCardTest {

    private void castUrborgJustice() {
        harness.setHand(player1, List.of(new UrborgJustice()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Opponent sacrifices one creature per creature put into the caster's graveyard")
    void sacrificesOnePerControllerDeath() {
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player1.getId(), 2, Integer::sum);
        harness.addToBattlefield(player2, new RedwoodTreefolk());
        harness.addToBattlefield(player2, new BenalishKnight());

        castUrborgJustice();

        harness.assertInGraveyard(player2, "Redwood Treefolk");
        harness.assertInGraveyard(player2, "Benalish Knight");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent chooses which creatures to sacrifice when they control more than died")
    void opponentChoosesWhenMoreCreaturesThanDeaths() {
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player1.getId(), 1, Integer::sum);
        harness.addToBattlefield(player2, new RedwoodTreefolk());
        harness.addToBattlefield(player2, new BenalishKnight());

        castUrborgJustice();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2,
                List.of(harness.getPermanentId(player2, "Benalish Knight")));

        harness.assertInGraveyard(player2, "Benalish Knight");
        harness.assertOnBattlefield(player2, "Redwood Treefolk");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Nothing is sacrificed when no creature entered the caster's graveyard")
    void noSacrificeWithoutDeaths() {
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player2.getId(), 3, Integer::sum);
        harness.addToBattlefield(player2, new RedwoodTreefolk());

        castUrborgJustice();

        harness.assertOnBattlefield(player2, "Redwood Treefolk");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Urborg Justice");
    }

    @Test
    @DisplayName("Only sacrifices creatures, not other permanents")
    void doesNotSacrificeNoncreaturePermanents() {
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player1.getId(), 1, Integer::sum);
        harness.addToBattlefield(player2, new RedwoodTreefolk());
        harness.addToBattlefield(player2, new MindStone());

        castUrborgJustice();

        harness.assertInGraveyard(player2, "Redwood Treefolk");
        harness.assertOnBattlefield(player2, "Mind Stone");
    }

    @Test
    @DisplayName("Cannot target the spell's controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new UrborgJustice()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void countsOwnedCreatureThatDiedUnderOpponentsControl() {
        BenalishKnight stolen = new BenalishKnight();
        stolen.setOwnerId(player1.getId());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, stolen);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, dying));
        harness.addToBattlefield(player2, new RedwoodTreefolk());

        castUrborgJustice();

        harness.assertInGraveyard(player1, "Benalish Knight");
        harness.assertInGraveyard(player2, "Redwood Treefolk");
    }

    @Test
    void doesNotCountOpponentsCreatureThatDiedUnderCastersControl() {
        BenalishKnight stolen = new BenalishKnight();
        stolen.setOwnerId(player2.getId());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, stolen);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, dying));
        harness.addToBattlefield(player2, new RedwoodTreefolk());

        castUrborgJustice();

        harness.assertInGraveyard(player2, "Benalish Knight");
        harness.assertOnBattlefield(player2, "Redwood Treefolk");
    }

    @Test
    void countsDeathsAfterCastingBeforeResolution() {
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new BenalishKnight());
        harness.addToBattlefield(player2, new RedwoodTreefolk());
        harness.setHand(player1, List.of(new UrborgJustice()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, dying));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Redwood Treefolk");
    }

    @Test
    void sacrificesAsManyAsPossibleWhenOpponentHasTooFewCreatures() {
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.put(player1.getId(), 3);
        harness.addToBattlefield(player2, new BenalishKnight());
        harness.addToBattlefield(player2, new MindStone());

        castUrborgJustice();

        harness.assertInGraveyard(player2, "Benalish Knight");
        harness.assertOnBattlefield(player2, "Mind Stone");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotCountCreatureCardsAlreadyInGraveyardWithoutBattlefieldDeaths() {
        harness.setGraveyard(player1, List.of(new BenalishKnight(), new RedwoodTreefolk()));
        harness.addToBattlefield(player2, new BenalishKnight());

        castUrborgJustice();

        harness.assertOnBattlefield(player2, "Benalish Knight");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({UrborgJustice.class, ZulaportCutthroat.class, BenalishKnight.class})
    void allSacrificedCreaturesDieSimultaneously() {
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.put(player1.getId(), 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new ZulaportCutthroat());
        harness.addToBattlefield(player2, new BenalishKnight());

        castUrborgJustice();

        harness.assertInGraveyard(player2, "Zulaport Cutthroat");
        harness.assertInGraveyard(player2, "Benalish Knight");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }
}
