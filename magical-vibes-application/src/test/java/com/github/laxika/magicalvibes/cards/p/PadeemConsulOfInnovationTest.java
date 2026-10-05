package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfBant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PadeemConsulOfInnovation.class, LeoninScimitar.class, ObeliskOfBant.class, Spellbook.class})
class PadeemConsulOfInnovationTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts you control have hexproof, but opponents' artifacts do not")
    void grantsHexproofToControlledArtifactsOnly() {
        harness.addToBattlefield(player1, new PadeemConsulOfInnovation());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        Permanent ownArtifact = findPermanent(player1, "Spellbook");
        Permanent opponentArtifact = findPermanent(player2, "Spellbook");

        assertThat(gqs.hasKeyword(gd, ownArtifact, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentArtifact, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Draws a card when your artifact is tied for the highest mana value")
    void drawsWhenArtifactIsTiedForHighestManaValue() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new PadeemConsulOfInnovation());
        harness.addToBattlefield(player1, new ObeliskOfBant());
        harness.addToBattlefield(player2, new ObeliskOfBant());

        advanceToAndResolveUpkeep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when an opponent controls a higher-mana-value artifact")
    void doesNotDrawWhenOpponentControlsHigherManaValueArtifact() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new PadeemConsulOfInnovation());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player2, new ObeliskOfBant());

        advanceToAndResolveUpkeep();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotGrantHexproofToNonartifactCreatures() {
        harness.addToBattlefield(player1, new PadeemConsulOfInnovation());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Padeem, Consul of Innovation"),
                Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void artifactsLoseHexproofWhenPadeemLeaves() {
        harness.addToBattlefield(player1, new PadeemConsulOfInnovation());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent artifact = findPermanent(player1, "Spellbook");
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HEXPROOF)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Padeem, Consul of Innovation"));

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void zeroManaValueArtifactQualifiesDespiteHigherManaValueNonartifact() {
        prepareDraw();
        harness.addToBattlefield(player1, new Spellbook());

        advanceToAndResolveUpkeep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerWithoutAnyArtifacts() {
        prepareDraw();

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerWhenOnlyOpponentControlsAnArtifact() {
        prepareDraw();
        harness.addToBattlefield(player2, new Spellbook());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenConditionBecomesTrueAfterUpkeepBegins() {
        prepareDraw();
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player2, new ObeliskOfBant());

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        gd.playerBattlefields.get(player2.getId()).remove(findPermanent(player2, "Obelisk of Bant"));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawWhenQualifyingArtifactLeavesBeforeResolution() {
        prepareDraw();
        harness.addToBattlefield(player1, new ObeliskOfBant());
        harness.addToBattlefield(player2, new LeoninScimitar());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Obelisk of Bant"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsWhenADifferentArtifactQualifiesAtResolution() {
        prepareDraw();
        harness.addToBattlefield(player1, new ObeliskOfBant());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player2, new LeoninScimitar());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Obelisk of Bant"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void alreadyTriggeredAbilityDrawsAfterPadeemLeaves() {
        prepareDraw();
        harness.addToBattlefield(player1, new Spellbook());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Padeem, Consul of Innovation"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        prepareDraw();
        harness.addToBattlefield(player1, new ObeliskOfBant());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void prepareDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new PadeemConsulOfInnovation()));
        harness.addToBattlefield(player1, new PadeemConsulOfInnovation());
    }

    private void advanceToAndResolveUpkeep() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
