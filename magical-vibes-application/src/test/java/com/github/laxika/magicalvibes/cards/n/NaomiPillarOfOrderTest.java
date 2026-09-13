package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AjanisMantra;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaomiPillarOfOrder.class, Spellbook.class, AjanisMantra.class})
class NaomiPillarOfOrderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a vigilant Samurai when its controller has an artifact and enchantment")
    void etbCreatesSamuraiWithArtifactAndEnchantment() {
        addQualifyingPermanents();
        harness.setHand(player1, List.of(new NaomiPillarOfOrder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
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
        harness.addToBattlefield(player1, new Spellbook());
        addCreatureReady(player1, new NaomiPillarOfOrder());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(samuraiTokens()).isEmpty();
    }

    @Test
    @DisplayName("Opponent-controlled artifacts and enchantments do not satisfy the condition")
    void opponentPermanentsDoNotSatisfyCondition() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new AjanisMantra());
        addCreatureReady(player1, new NaomiPillarOfOrder());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(samuraiTokens()).isEmpty();
    }

    private void addQualifyingPermanents() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new AjanisMantra());
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
