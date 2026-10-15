package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FellwarStone;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.cards.n.NinjasKunai;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PatchworkAutomaton;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ScrapWelder.class, FellwarStone.class, Ornithopter.class,
        NetworkTerminal.class, NinjasKunai.class, PatchworkAutomaton.class})
class ScrapWelderTest extends BaseCardTest {

    @Test
    void returnsNoncreatureArtifactAndPaysTapCost() {
        Permanent welder = addCreatureReady(player1, new ScrapWelder());
        harness.addToBattlefield(player1, new PatchworkAutomaton());
        Card target = new NinjasKunai();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        assertThat(welder.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Patchwork Automaton");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ninja's Kunai");
        harness.assertNotInGraveyard(player1, "Ninja's Kunai");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Ninja's Kunai"), Keyword.HASTE)).isTrue();
    }

    @Test
    void usesChosenSacrificesManaValueWhenSeveralArtifactsAreAvailable() {
        addCreatureReady(player1, new ScrapWelder());
        Permanent cheaper = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        Card target = new PatchworkAutomaton();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cheaper).doesNotContain(fodder);
        harness.assertInGraveyard(player1, "Network Terminal");
        harness.assertOnBattlefield(player1, "Patchwork Automaton");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Patchwork Automaton"), Keyword.HASTE)).isTrue();
    }

    @Test
    void cannotActivateWithoutAnArtifactToSacrifice() {
        Permanent welder = addCreatureReady(player1, new ScrapWelder());
        Card target = new NinjasKunai();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(welder.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Ninja's Kunai");
    }

    @Test
    @DisplayName("Sacrifices an artifact and returns a cheaper artifact with haste")
    void sacrificesArtifactAndReturnsCheaperArtifactWithHaste() {
        addCreatureReady(player1, new ScrapWelder());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new FellwarStone());
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Ornithopter");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Rejects an artifact whose mana value equals the sacrificed artifact")
    void rejectsArtifactWithEqualManaValue() {
        addCreatureReady(player1, new ScrapWelder());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new FellwarStone());
        Card target = new FellwarStone();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertInGraveyard(player1, "Fellwar Stone");
    }

    @Test
    @DisplayName("Returned artifact loses the temporary haste at end of turn")
    void returnedArtifactLosesHasteAtEndOfTurn() {
        addCreatureReady(player1, new ScrapWelder());
        harness.addToBattlefieldAndReturn(player1, new FellwarStone());
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Ornithopter");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
    }

    @Test
    void rejectsArtifactInOpponentsGraveyard() {
        addCreatureReady(player1, new ScrapWelder());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new FellwarStone());
        Card target = new Ornithopter();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    void rejectsNonartifactTarget() {
        addCreatureReady(player1, new ScrapWelder());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new FellwarStone());
        Card target = new ScrapWelder();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertInGraveyard(player1, "Scrap Welder");
    }

    @Test
    void zeroManaValueSacrificeCannotReturnZeroManaValueArtifact() {
        addCreatureReady(player1, new ScrapWelder());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    void targetLeavingGraveyardDoesNotRefundSacrifice() {
        addCreatureReady(player1, new ScrapWelder());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new FellwarStone());
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of(fodder.getCard()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Fellwar Stone");
        harness.assertInGraveyard(player1, "Fellwar Stone");
    }

    @Test
    void summoningSickWelderCannotActivateTapAbility() {
        harness.addToBattlefield(player1, new ScrapWelder());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new FellwarStone());
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertInGraveyard(player1, "Ornithopter");
    }
}
