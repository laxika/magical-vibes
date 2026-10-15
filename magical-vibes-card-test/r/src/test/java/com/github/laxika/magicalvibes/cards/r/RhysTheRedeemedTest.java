package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhysTheRedeemed.class, GrizzlyBears.class})
class RhysTheRedeemedTest extends BaseCardTest {

    private List<Permanent> creatureTokensNamed(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals(name))
                .toList();
    }

    private Permanent addCreatureToken(Player player, String name) {
        Card card = new Card();
        card.setToken(true);
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.ELF, CardSubtype.WARRIOR));
        return addCreatureReady(player, card);
    }

    @Test
    @DisplayName("First ability creates a 1/1 Elf Warrior token")
    void firstAbilityCreatesToken() {
        addCreatureReady(player1, new RhysTheRedeemed());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = creatureTokensNamed(player1, "Elf Warrior");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getCard().getPower()).isEqualTo(1);
        assertThat(tokens.get(0).getCard().getToughness()).isEqualTo(1);
        assertThat(tokens.get(0).getCard().getSubtypes())
                .contains(CardSubtype.ELF, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Second ability creates a copy of each creature token you control")
    void secondAbilityCopiesEachCreatureToken() {
        addCreatureReady(player1, new RhysTheRedeemed());
        addCreatureToken(player1, "Elf Warrior");
        addCreatureToken(player1, "Elf Warrior");
        // A non-token creature must not be copied.
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Two originals + two copies; the snapshot means copies aren't themselves copied.
        assertThat(creatureTokensNamed(player1, "Elf Warrior")).hasSize(4);
        // The non-token creature is untouched (still exactly one Grizzly Bears).
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Second ability with no creature tokens creates nothing")
    void secondAbilityWithNoTokensCreatesNothing() {
        addCreatureReady(player1, new RhysTheRedeemed());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 6);

        int before = gd.playerBattlefields.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(before);
    }

    @Test
    void whiteManaPaysFirstAbilityAndTokenHasBothColors() {
        Permanent rhys = addCreatureReady(player1, new RhysTheRedeemed());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(rhys.isTapped()).isTrue();
        assertThat(creatureTokensNamed(player1, "Elf Warrior")).isEmpty();
        harness.passBothPriorities();

        assertThat(creatureTokensNamed(player1, "Elf Warrior")).hasSize(1);
        assertThat(findPermanent(player1, "Elf Warrior").getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
    }

    @Test
    void secondAbilityCopiesRealTokenWithoutCountersOrTappedState() {
        Permanent rhys = addCreatureReady(player1, new RhysTheRedeemed());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent original = findPermanent(player1, "Elf Warrior");
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        original.tap();
        rhys.untap();
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(rhys.isTapped()).isTrue();
        harness.passBothPriorities();

        List<Permanent> tokens = creatureTokensNamed(player1, "Elf Warrior");
        assertThat(tokens).hasSize(2);
        Permanent copy = tokens.stream().filter(p -> !p.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getCard().getPower()).isEqualTo(1);
        assertThat(copy.getCard().getToughness()).isEqualTo(1);
        assertThat(copy.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(copy.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.WARRIOR);
    }

    @Test
    void secondAbilityIgnoresOpposingCreatureTokensAndNoncreatureTokens() {
        addCreatureReady(player1, new RhysTheRedeemed());
        addCreatureToken(player1, "Elf Warrior");
        addCreatureToken(player2, "Elf Warrior");
        Card artifactToken = new Card();
        artifactToken.setToken(true);
        artifactToken.setName("Artifact token");
        artifactToken.setType(CardType.ARTIFACT);
        addCreatureReady(player1, artifactToken);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(creatureTokensNamed(player1, "Elf Warrior")).hasSize(2);
        assertThat(creatureTokensNamed(player2, "Elf Warrior")).hasSize(1);
        assertThat(findPermanents(player1, "Artifact token")).hasSize(1);
    }

    @Test
    void secondAbilityUsesTokensPresentAtResolutionAndSurvivesSourceLeaving() {
        Permanent rhys = addCreatureReady(player1, new RhysTheRedeemed());
        Permanent departed = addCreatureToken(player1, "Departed token");
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(departed);
        gd.playerBattlefields.get(player1.getId()).remove(rhys);
        addCreatureToken(player1, "New token");
        harness.passBothPriorities();

        assertThat(creatureTokensNamed(player1, "Departed token")).isEmpty();
        assertThat(creatureTokensNamed(player1, "New token")).hasSize(2);
    }
}
