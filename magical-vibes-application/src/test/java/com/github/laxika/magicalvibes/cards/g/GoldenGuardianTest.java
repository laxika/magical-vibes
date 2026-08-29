package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoldenGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Returns transformed after fighting and dying during the activated ability")
    void returnsTransformedWhenItDiesAfterFight() {
        Permanent guardian = addGuardianReady(player1);
        Permanent dreadmaw = addCreatureReady(player1, new ColossalDreadmaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, dreadmaw.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent garrison = findPermanent(player1, "Gold-Forge Garrison");
        assertThat(garrison.isTransformed()).isTrue();
        assertThat(garrison.getOriginalCard()).isSameAs(guardian.getOriginalCard());
        assertThat(findPermanentOrNull(player1, "Colossal Dreadmaw")).isNotNull();
        harness.assertNotInGraveyard(player1, "Golden Guardian");
    }

    @Test
    @DisplayName("Rejects a target not controlled by the activating player")
    void targetMustBeAnotherCreatureYouControl() {
        Permanent guardian = addGuardianReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new ColossalDreadmaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                0,
                0,
                null,
                opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
        assertThat(guardian.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Back face adds two mana of the chosen color")
    void backFaceAddsTwoMana() {
        Permanent garrison = addGarrisonReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(garrison.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Back face creates a 4/4 colorless artifact Golem token")
    void backFaceCreatesGolemToken() {
        addGarrisonReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().hasType(CardType.ARTIFACT)
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getName().equals("Golem")
                        && gqs.getEffectivePower(gd, permanent) == 4
                        && gqs.getEffectiveToughness(gd, permanent) == 4);
    }

    private Permanent addGuardianReady(Player player) {
        Permanent guardian = new Permanent(new GoldenGuardian());
        guardian.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(guardian);
        return guardian;
    }

    private Permanent addGarrisonReady(Player player) {
        GoldenGuardian card = new GoldenGuardian();
        Permanent garrison = new Permanent(card);
        garrison.setCard(card.getBackFaceCard());
        garrison.setTransformed(true);
        garrison.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(garrison);
        return garrison;
    }

    private Permanent addCreatureReady(Player player, ColossalDreadmaw card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent findPermanentOrNull(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .findFirst()
                .orElse(null);
    }
}
