package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FaerieTauntings;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.p.Peppersmoke;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScionOfOona.class, SpellstutterSprite.class, GoldmeadowStalwart.class, FaerieTauntings.class, Peppersmoke.class})
class ScionOfOonaTest extends BaseCardTest {

    @Test
    @DisplayName("Other Faerie creatures you control get +1/+1 and shroud")
    void buffsOtherFaeriesYouControl() {
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player1, new SpellstutterSprite());

        Permanent sprite = findPermanent(player1, "Spellstutter Sprite");

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Scion of Oona does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new ScionOfOona());

        Permanent scion = findPermanent(player1, "Scion of Oona");

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, scion, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Faerie creatures")
    void doesNotBuffNonFaeries() {
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player1, new GoldmeadowStalwart());

        Permanent stalwart = findPermanent(player1, "Goldmeadow Stalwart");

        assertThat(gqs.getEffectivePower(gd, stalwart)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stalwart)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, stalwart, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Faerie creatures")
    void doesNotBuffOpponentFaeries() {
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player2, new SpellstutterSprite());

        Permanent opponentSprite = findPermanent(player2, "Spellstutter Sprite");

        assertThat(gqs.getEffectivePower(gd, opponentSprite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSprite)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentSprite, Keyword.SHROUD)).isFalse();
    }

    // ===== Multiple Scions =====

    @Test
    @DisplayName("Two Scions of Oona buff each other")
    void twoScionsBuffEachOther() {
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player1, new ScionOfOona());

        List<Permanent> scions = findPermanents(player1, "Scion of Oona");

        assertThat(scions).hasSize(2);
        for (Permanent scion : scions) {
            assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, scion, Keyword.SHROUD)).isTrue();
        }
    }

    @Test
    @DisplayName("Two Scions give +2/+2 to other Faeries")
    void twoScionsStackBonuses() {
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player1, new SpellstutterSprite());

        Permanent sprite = findPermanent(player1, "Spellstutter Sprite");

        // 1/1 base + 1/1 from each Scion = 3/3
        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.SHROUD)).isTrue();
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Scion of Oona leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent scion = harness.addToBattlefieldAndReturn(player1, new ScionOfOona());
        harness.addToBattlefield(player1, new SpellstutterSprite());

        Permanent sprite = findPermanent(player1, "Spellstutter Sprite");

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.SHROUD)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, scion));

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Bonus applies when Scion of Oona resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new SpellstutterSprite());

        Permanent sprite = findPermanent(player1, "Spellstutter Sprite");
        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.SHROUD)).isFalse();

        harness.setHand(player1, List.of(new ScionOfOona()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sprite, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Other Faerie permanents have shroud")
    void grantsShroudToOtherFaeriePermanents() {
        harness.addToBattlefield(player1, new ScionOfOona());
        harness.addToBattlefield(player1, new FaerieTauntings());

        Permanent faerieTauntings = findPermanent(player1, "Faerie Tauntings");

        assertThat(gqs.hasKeyword(gd, faerieTauntings, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Flashing in Scion on an opponent's turn makes a targeted Faerie an illegal target")
    void flashProtectsFaerieFromSpellAlreadyOnStack() {
        harness.forceActivePlayer(player2);
        Permanent sprite = harness.addToBattlefieldAndReturn(player1, new SpellstutterSprite());
        harness.setHand(player2, List.of(new Peppersmoke()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, sprite.getId());

        harness.setHand(player1, List.of(new ScionOfOona()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scion of Oona");
        harness.assertOnBattlefield(player1, "Spellstutter Sprite");
        harness.assertInGraveyard(player2, "Peppersmoke");
        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's noncreature Faeries do not gain shroud")
    void doesNotGrantShroudToOpponentFaeriePermanents() {
        harness.addToBattlefield(player1, new ScionOfOona());
        Permanent tauntings = harness.addToBattlefieldAndReturn(player2, new FaerieTauntings());

        assertThat(gqs.hasKeyword(gd, tauntings, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents the Faerie's controller from targeting it too")
    void shroudPreventsControllerTargeting() {
        harness.addToBattlefield(player1, new ScionOfOona());
        Permanent sprite = harness.addToBattlefieldAndReturn(player1, new SpellstutterSprite());
        harness.setHand(player1, List.of(new Peppersmoke()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, sprite.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
