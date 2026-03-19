package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AryelKnightOfWindgraceTest extends BaseCardTest {

    @Test
    @DisplayName("First ability creates a 2/2 white Knight token with vigilance")
    void firstAbilityCreatesKnightToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new AryelKnightOfWindgrace());
        Permanent aryel = findPermanent(player1, "Aryel, Knight of Windgrace");
        aryel.setSummoningSick(false);

        harness.addMana(player1, ManaColor.WHITE, 3);

        int aryelIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aryel);
        harness.activateAbility(player1, aryelIdx, 0, null, null);
        harness.passBothPriorities();

        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight"))
                .findFirst().orElseThrow();
        assertThat(knight.getCard().getPower()).isEqualTo(2);
        assertThat(knight.getCard().getToughness()).isEqualTo(2);
        assertThat(knight.getCard().getSubtypes()).contains(CardSubtype.KNIGHT);
        assertThat(knight.getCard().getKeywords()).contains(Keyword.VIGILANCE);
        assertThat(knight.getCard().getColor()).isEqualTo(CardColor.WHITE);
    }

    @Test
    @DisplayName("First ability taps Aryel")
    void firstAbilityTapsAryel() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new AryelKnightOfWindgrace());
        Permanent aryel = findPermanent(player1, "Aryel, Knight of Windgrace");
        aryel.setSummoningSick(false);

        harness.addMana(player1, ManaColor.WHITE, 3);

        int aryelIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aryel);
        harness.activateAbility(player1, aryelIdx, 0, null, null);

        assertThat(aryel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("First ability requires {2}{W} mana")
    void firstAbilityRequiresMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new AryelKnightOfWindgrace());
        Permanent aryel = findPermanent(player1, "Aryel, Knight of Windgrace");
        aryel.setSummoningSick(false);

        // Only 1 white mana — not enough for {2}{W}
        harness.addMana(player1, ManaColor.WHITE, 1);

        int aryelIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aryel);
        assertThatThrownBy(() -> harness.activateAbility(player1, aryelIdx, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability destroys target creature with power X or less (auto-tap Knights)")
    void secondAbilityDestroysCreatureAutoTap() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new AryelKnightOfWindgrace());
        Permanent aryel = findPermanent(player1, "Aryel, Knight of Windgrace");
        aryel.setSummoningSick(false);

        // Add exactly 2 untapped Knights
        Permanent knight1 = addReadyKnight(player1);
        Permanent knight2 = addReadyKnight(player1);

        // Add target creature for player2
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.addMana(player1, ManaColor.BLACK, 1);

        int aryelIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aryel);
        UUID targetId = bears.getId();

        // X=2, tap 2 Knights -> destroy creature with power 2 or less
        harness.activateAbility(player1, aryelIdx, 1, 2, targetId);
        harness.passBothPriorities();

        // Grizzly Bears (2/2) should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Knights should be tapped as cost
        assertThat(knight1.isTapped()).isTrue();
        assertThat(knight2.isTapped()).isTrue();
        // Aryel should also be tapped
        assertThat(aryel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability fails if target creature power exceeds X")
    void secondAbilityFailsIfPowerExceedsX() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new AryelKnightOfWindgrace());
        Permanent aryel = findPermanent(player1, "Aryel, Knight of Windgrace");
        aryel.setSummoningSick(false);

        Permanent knight1 = addReadyKnight(player1);

        // Add a 3/3 creature
        Card bigCreature = new GrizzlyBears();
        bigCreature.setPower(3);
        bigCreature.setToughness(3);
        Permanent bigPerm = new Permanent(bigCreature);
        gd.playerBattlefields.get(player2.getId()).add(bigPerm);

        harness.addMana(player1, ManaColor.BLACK, 1);

        int aryelIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aryel);
        UUID targetId = bigPerm.getId();

        // X=1, only 1 Knight tapped -> can only destroy power 1 or less
        assertThatThrownBy(() -> harness.activateAbility(player1, aryelIdx, 1, 1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability with X=0 can destroy a 0-power creature")
    void secondAbilityWithXZero() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new AryelKnightOfWindgrace());
        Permanent aryel = findPermanent(player1, "Aryel, Knight of Windgrace");
        aryel.setSummoningSick(false);

        // Add a 0/4 wall
        Card wall = new GrizzlyBears();
        wall.setPower(0);
        wall.setToughness(4);
        Permanent wallPerm = new Permanent(wall);
        gd.playerBattlefields.get(player2.getId()).add(wallPerm);

        harness.addMana(player1, ManaColor.BLACK, 1);

        int aryelIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aryel);
        UUID targetId = wallPerm.getId();

        // X=0, no Knights tapped, destroy creature with power 0 or less
        harness.activateAbility(player1, aryelIdx, 1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(wallPerm.getId()));
    }

    @Test
    @DisplayName("Second ability fails if not enough untapped Knights for X")
    void secondAbilityFailsIfNotEnoughKnights() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new AryelKnightOfWindgrace());
        Permanent aryel = findPermanent(player1, "Aryel, Knight of Windgrace");
        aryel.setSummoningSick(false);

        // Only 1 Knight
        addReadyKnight(player1);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.addMana(player1, ManaColor.BLACK, 1);

        int aryelIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aryel);
        UUID targetId = bears.getId();

        // X=2 but only 1 Knight available
        assertThatThrownBy(() -> harness.activateAbility(player1, aryelIdx, 1, 2, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability interactive Knight choice when more Knights than X")
    void secondAbilityInteractiveKnightChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new AryelKnightOfWindgrace());
        Permanent aryel = findPermanent(player1, "Aryel, Knight of Windgrace");
        aryel.setSummoningSick(false);

        // 3 Knights, but X=2
        Permanent knight1 = addReadyKnight(player1);
        Permanent knight2 = addReadyKnight(player1);
        Permanent knight3 = addReadyKnight(player1);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.addMana(player1, ManaColor.BLACK, 1);

        int aryelIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aryel);
        UUID targetId = bears.getId();

        // X=2: need to choose 2 of 3 Knights
        harness.activateAbility(player1, aryelIdx, 1, 2, targetId);

        // Choose knight1 and knight2
        harness.handlePermanentChosen(player1, knight1.getId());
        harness.handlePermanentChosen(player1, knight2.getId());

        harness.passBothPriorities();

        // Bears destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Chosen knights tapped, unchosen untapped
        assertThat(knight1.isTapped()).isTrue();
        assertThat(knight2.isTapped()).isTrue();
        assertThat(knight3.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Second ability cannot activate with summoning sickness")
    void secondAbilityCannotActivateWithSummoningSickness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new AryelKnightOfWindgrace());
        // Aryel has summoning sickness (default)

        addReadyKnight(player1);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability excludes Aryel from Knights to tap (excludeSource)")
    void secondAbilityExcludesAryelFromKnightsTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new AryelKnightOfWindgrace());
        Permanent aryel = findPermanent(player1, "Aryel, Knight of Windgrace");
        aryel.setSummoningSick(false);

        // No other Knights — Aryel is a Knight but excluded as source
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.addMana(player1, ManaColor.BLACK, 1);

        int aryelIdx = gd.playerBattlefields.get(player1.getId()).indexOf(aryel);
        UUID targetId = bears.getId();

        // X=1 but no other Knights available
        assertThatThrownBy(() -> harness.activateAbility(player1, aryelIdx, 1, 1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyKnight(Player player) {
        Card knightCard = new GrizzlyBears();
        knightCard.setSubtypes(List.of(CardSubtype.KNIGHT));
        Permanent perm = new Permanent(knightCard);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
