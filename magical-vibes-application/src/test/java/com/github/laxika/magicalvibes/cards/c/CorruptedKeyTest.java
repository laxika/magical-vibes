package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorruptedKey.class, GrizzlyBears.class, EnsoulArtifact.class})
class CorruptedKeyTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Corrupted Key gives your creatures menace and deathtouch")
    void tappedKeyGrantsMenaceAndDeathtouchToYourCreatures() {
        Permanent key = harness.addToBattlefieldAndReturn(player1, new CorruptedKey());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        key.tap();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Your creatures lose the granted keywords when Corrupted Key becomes untapped")
    void untappedKeyDoesNotGrantKeywords() {
        Permanent key = harness.addToBattlefieldAndReturn(player1, new CorruptedKey());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        key.tap();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isTrue();

        key.untap();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Tapped Corrupted Key grants keywords to creatures entering later")
    void grantsKeywordsToCreaturesEnteringWhileTapped() {
        Permanent key = harness.addToBattlefieldAndReturn(player1, new CorruptedKey());
        key.tap();

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Another tapped Key continues granting keywords when one becomes untapped")
    void anotherTappedKeyKeepsKeywordsActive() {
        Permanent firstKey = harness.addToBattlefieldAndReturn(player1, new CorruptedKey());
        Permanent secondKey = harness.addToBattlefieldAndReturn(player1, new CorruptedKey());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        firstKey.tap();
        secondKey.tap();

        firstKey.untap();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        secondKey.untap();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("A tapped Corrupted Key animated by Ensoul Artifact grants itself both keywords")
    void animatedTappedKeyGrantsItselfKeywords() {
        Permanent key = harness.addToBattlefieldAndReturn(player1, new CorruptedKey());
        key.tap();
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, key.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, key)).isTrue();
        assertThat(gqs.hasKeyword(gd, key, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, key, Keyword.DEATHTOUCH)).isTrue();

        key.untap();

        assertThat(gqs.hasKeyword(gd, key, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, key, Keyword.DEATHTOUCH)).isFalse();
    }
}
