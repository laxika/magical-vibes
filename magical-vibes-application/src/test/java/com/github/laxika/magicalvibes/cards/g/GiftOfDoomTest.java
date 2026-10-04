package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.StratusDancer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiftOfDoom.class, GrizzlyBears.class, StratusDancer.class})
class GiftOfDoomTest extends BaseCardTest {

    @Test
    @CardUsed({GiftOfDoom.class, StratusDancer.class})
    void castingFaceUpAttachesAndGrantsBothKeywords() {
        Permanent target = addCreatureReady(player2, new StratusDancer());
        harness.setHand(player1, List.of(new GiftOfDoom()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent gift = findPermanent(player1, "Gift of Doom");
        assertThat(gift.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @CardUsed({GiftOfDoom.class, StratusDancer.class})
    void sacrificingTheOnlyOtherCreatureLeavesNoLegalHost() {
        Permanent sacrifice = addCreatureReady(player1, new StratusDancer());
        Permanent gift = castFaceDown();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gift),
                List.of(sacrifice.getId()));

        harness.assertInGraveyard(player1, "Stratus Dancer");
        harness.assertInGraveyard(player1, "Gift of Doom");
        harness.assertNotOnBattlefield(player1, "Gift of Doom");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turningFaceUpSacrificesAnotherCreatureAndMayAttachAura() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent gift = castFaceDown();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gift),
                List.of(sacrifice.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gift.isFaceDown()).isFalse();
        assertThat(gift.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void decliningAttachmentLeavesAuraUnattachedWithoutGrantingKeywords() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent gift = castFaceDown();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gift),
                List.of(sacrifice.getId()));
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gift.isFaceDown()).isFalse();
        assertThat(gift.isAttached()).isFalse();
        harness.assertInGraveyard(player1, "Gift of Doom");
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void morphCannotSacrificeGiftOfDoomItself() {
        Permanent gift = castFaceDown();

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(gift), List.of(gift.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required filter");
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new GiftOfDoom()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Gift of Doom");
    }

    @Test
    void attachmentDoesNotTargetAndCanEnchantAnOpponentsHexproofCreature() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.getPersistentGrantedKeywords().add(Keyword.HEXPROOF);
        Permanent gift = castFaceDown();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gift),
                List.of(sacrifice.getId()));

        assertThat(gd.stack).isEmpty();
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gift.getAttachedTo()).isEqualTo(target.getId());
        harness.assertOnBattlefield(player1, "Gift of Doom");
    }
}
