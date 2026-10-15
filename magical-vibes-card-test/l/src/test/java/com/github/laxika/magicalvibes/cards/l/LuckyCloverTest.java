package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.d.DidntSayPlease;
import com.github.laxika.magicalvibes.cards.h.HeartsDesire;
import com.github.laxika.magicalvibes.cards.s.Stomp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LuckyClover.class, LovestruckBeast.class, HeartsDesire.class, BonecrusherGiant.class, Stomp.class, DidntSayPlease.class})
class LuckyCloverTest extends BaseCardTest {

    @Test
    void copiesAdventureSpell() {
        harness.addToBattlefield(player1, new LuckyClover());
        harness.setHand(player1, List.of(new LovestruckBeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        resolveAllTriggers();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())
                .filter(Card::isToken)
                .count();
        assertThat(tokenCount).isEqualTo(2);
    }

    @Test
    void doesNotCopyNormalCreatureCast() {
        harness.addToBattlefield(player1, new LuckyClover());
        harness.setHand(player1, List.of(new LovestruckBeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void eachCloverCopiesOnceWithoutTriggeringOnCopies() {
        harness.addToBattlefield(player1, new LuckyClover());
        harness.addToBattlefield(player1, new LuckyClover());
        LovestruckBeast beast = new LovestruckBeast();
        harness.setHand(player1, List.of(beast));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).count()).isEqualTo(3);
        assertThat(gd.findExiledCard(beast.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsOnlyKeys(beast.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCopyOpponentsAdventure() {
        harness.addToBattlefield(player1, new LuckyClover());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new LovestruckBeast()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAdventure(player2, 0, List.of());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).count()).isEqualTo(1);
    }

    @Test
    void decliningNewTargetsStillResolvesMandatoryCopy() {
        harness.addToBattlefield(player1, new LuckyClover());
        harness.setHand(player1, List.of(new BonecrusherGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }

    @Test
    void canChooseNewPlayerTargetForAdventureCopy() {
        harness.addToBattlefield(player1, new LuckyClover());
        harness.setHand(player1, List.of(new BonecrusherGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void stillCopiesAdventureAfterOriginalIsCountered() {
        harness.addToBattlefield(player1, new LuckyClover());
        LovestruckBeast beast = new LovestruckBeast();
        harness.setHand(player1, List.of(beast, new DidntSayPlease()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.castAndResolveInstant(player1, 0, beast.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).count()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Lovestruck Beast");
        assertThat(gd.findExiledCard(beast.getId())).isNull();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }
}
