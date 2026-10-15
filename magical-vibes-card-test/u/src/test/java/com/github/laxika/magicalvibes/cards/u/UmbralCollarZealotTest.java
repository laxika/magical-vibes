package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UmbralCollarZealot.class, GrizzlyBears.class, Spellbook.class, Forest.class})
class UmbralCollarZealotTest extends BaseCardTest {

    @Test
    void sacrificesAnotherCreatureAndSurveilsToGraveyard() {
        Permanent zealot = addCreatureReady(player1, new UmbralCollarZealot());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zealot).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
    }

    @Test
    void sacrificesAnotherArtifactAndKeepsTopCard() {
        Permanent zealot = addCreatureReady(player1, new UmbralCollarZealot());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zealot).doesNotContain(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void cannotSacrificeItself() {
        Permanent zealot = addCreatureReady(player1, new UmbralCollarZealot());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zealot);
    }

    @Test
    void canActivateWhileSummoningSickAndPaysSacrificeBeforeResolution() {
        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new UmbralCollarZealot());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest topCard = new Forest();
        Forest secondCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zealot).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard()).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard).doesNotContain(secondCard);
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new UmbralCollarZealot());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zealot).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotSacrificeAnOpponentsCreatureOrArtifact() {
        Permanent zealot = addCreatureReady(player1, new UmbralCollarZealot());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zealot);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature, artifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeANoncreatureNonartifactLand() {
        Permanent zealot = addCreatureReady(player1, new UmbralCollarZealot());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zealot, land);
        assertThat(gd.stack).isEmpty();
    }
}
