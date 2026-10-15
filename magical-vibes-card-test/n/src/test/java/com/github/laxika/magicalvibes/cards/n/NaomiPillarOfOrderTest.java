package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.SpiritedCompanion;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaomiPillarOfOrder.class, NetworkTerminal.class, SpiritedCompanion.class})
class NaomiPillarOfOrderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a vigilant Samurai when its controller has an artifact and enchantment")
    void etbCreatesSamuraiWithArtifactAndEnchantment() {
        addQualifyingPermanents();
        harness.castFromHand(player1, new NaomiPillarOfOrder(), "{3}{W}{B}");
        resolveAllTriggers();

        assertSamuraiCreated();
    }

    @Test
    @DisplayName("Attacking creates a vigilant Samurai when its controller has an artifact and enchantment")
    void attackCreatesSamuraiWithArtifactAndEnchantment() {
        addQualifyingPermanents();
        addCreatureReady(player1, new NaomiPillarOfOrder());

        declareAttackers(List.of(2));
        resolveAllTriggers();

        assertSamuraiCreated();
    }

    @Test
    @DisplayName("Does not create a Samurai without both an artifact and an enchantment")
    void doesNotCreateSamuraiWithoutBothPermanentTypes() {
        harness.addToBattlefield(player1, new NetworkTerminal());
        addCreatureReady(player1, new NaomiPillarOfOrder());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(samuraiTokens()).isEmpty();
    }

    @Test
    @DisplayName("Opponent-controlled artifacts and enchantments do not satisfy the condition")
    void opponentPermanentsDoNotSatisfyCondition() {
        harness.addToBattlefield(player2, new NetworkTerminal());
        harness.addToBattlefield(player2, new SpiritedCompanion());
        addCreatureReady(player1, new NaomiPillarOfOrder());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(samuraiTokens()).isEmpty();
    }

    private void addQualifyingPermanents() {
        harness.addToBattlefield(player1, new NetworkTerminal());
        harness.addToBattlefield(player1, new SpiritedCompanion());
    }

    @Test
    void etbCreatesAnUntappedWhiteSamuraiCreatureToken() {
        addQualifyingPermanents();
        harness.enterBattlefieldAndReturn(player1, new NaomiPillarOfOrder());
        resolveAllTriggers();

        assertSamuraiCreated();
        Permanent token = samuraiTokens().getFirst();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAMURAI);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
    }

    @Test
    void etbDoesNotTriggerWithoutAnArtifact() {
        harness.addToBattlefield(player1, new SpiritedCompanion());
        harness.enterBattlefieldAndReturn(player1, new NaomiPillarOfOrder());

        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();
        assertThat(samuraiTokens()).isEmpty();
    }

    @Test
    void attackDoesNotTriggerWithoutAnEnchantmentEvenIfOneArrivesLater() {
        harness.addToBattlefield(player1, new NetworkTerminal());
        addCreatureReady(player1, new NaomiPillarOfOrder());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new SpiritedCompanion());
        resolveAllTriggers();
        assertThat(samuraiTokens()).isEmpty();
    }

    @Test
    void etbDoesNothingIfArtifactLeavesBeforeResolution() {
        addQualifyingPermanents();
        harness.enterBattlefieldAndReturn(player1, new NaomiPillarOfOrder());
        assertThat(gd.stack).hasSize(1);

        Permanent artifact = findPermanent(player1, "Network Terminal");
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());
        resolveAllTriggers();

        assertThat(samuraiTokens()).isEmpty();
    }

    @Test
    void attackDoesNothingIfEnchantmentLeavesBeforeResolution() {
        addQualifyingPermanents();
        addCreatureReady(player1, new NaomiPillarOfOrder());
        declareAttackers(List.of(2));
        assertThat(gd.stack).hasSize(1);

        Permanent enchantment = findPermanent(player1, "Spirited Companion");
        gd.playerBattlefields.get(player1.getId()).remove(enchantment);
        gd.playerGraveyards.get(player1.getId()).add(enchantment.getCard());
        resolveAllTriggers();

        assertThat(samuraiTokens()).isEmpty();
    }

    @Test
    void attackStillCreatesTokenIfNaomiLeavesBeforeResolution() {
        addQualifyingPermanents();
        Permanent naomi = addCreatureReady(player1, new NaomiPillarOfOrder());
        declareAttackers(List.of(2));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(naomi);
        gd.playerGraveyards.get(player1.getId()).add(naomi.getCard());
        resolveAllTriggers();

        assertSamuraiCreated();
        assertThat(samuraiTokens()).allSatisfy(token -> {
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isAttacking()).isFalse();
        });
    }

    private void assertSamuraiCreated() {
        assertThat(samuraiTokens()).hasSize(1).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getKeywords()).contains(Keyword.VIGILANCE);
        });
    }

    private List<Permanent> samuraiTokens() {
        return findPermanents(player1, "Samurai").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
