package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.r.RiteOfReplication;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpringleafParade.class, Opalescence.class, RiteOfReplication.class})
class SpringleafParadeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X 1/1 colorless Shapeshifter tokens with changeling")
    void createsXChangelingTokens() {
        castForX(2);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
            assertThat(token.getCard().getKeywords()).contains(Keyword.CHANGELING);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Creature tokens you control can add one mana of any color")
    void creatureTokensCanTapForAnyColor() {
        castForX(1);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        token.setSummoningSick(false);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(token), null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(token.isTapped()).isTrue();
    }

    @Test
    @DisplayName("X zero creates no tokens but leaves the enchantment on the battlefield")
    void zeroCreatesNoTokens() {
        castForX(0);

        harness.assertOnBattlefield(player1, "Springleaf Parade");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Entering without being cast creates no tokens")
    void enteringWithoutBeingCastCreatesNoTokens() {
        harness.enterBattlefieldAndReturn(player1, new SpringleafParade());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Springleaf Parade");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("New tokens cannot tap for mana while summoning sick")
    void newTokensCannotTapForMana() {
        castForX(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(token), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(token.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Tokens lose the granted mana ability when the enchantment leaves")
    void tokensLoseManaAbilityWhenEnchantmentLeaves() {
        castForX(1);
        Permanent enchantment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getCard().isToken())
                .findFirst().orElseThrow();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        token.setSummoningSick(false);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantment));

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(token), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(token.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("The entry trigger still creates X tokens after the enchantment leaves")
    void entryTriggerResolvesAfterEnchantmentLeaves() {
        harness.setHand(player1, List.of(new SpringleafParade()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 3);
        Permanent enchantment = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantment));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3)
                .allMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("An animated token copy grants its mana ability to itself")
    void animatedTokenCopyGrantsManaAbilityToItself() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent original = harness.addToBattlefieldAndReturn(player2, new SpringleafParade());
        harness.setHand(player1, List.of(new RiteOfReplication()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, original.getId());
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        token.setSummoningSick(false);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(token), null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(token.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private void castForX(int xValue) {
        harness.setHand(player1, List.of(new SpringleafParade()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castAndResolveSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }
}
