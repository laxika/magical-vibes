package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirliftChaplain;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.s.StaticNet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoranOfTheThirdPath.class, StaticNet.class, EnergyRefractor.class, AirliftChaplain.class})
class LoranOfTheThirdPathTest extends BaseCardTest {

    @Test
    @DisplayName("When Loran enters, it destroys an artifact")
    void entersAndDestroysArtifact() {
        harness.addToBattlefield(player2, new EnergyRefractor());
        UUID targetId = harness.getPermanentId(player2, "Energy Refractor");
        castLoran(List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Energy Refractor");
        harness.assertInGraveyard(player2, "Energy Refractor");
    }

    @Test
    @DisplayName("When Loran enters, it destroys an enchantment")
    void entersAndDestroysEnchantment() {
        harness.addToBattlefield(player2, new StaticNet());
        UUID targetId = harness.getPermanentId(player2, "Static Net");
        castLoran(List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Static Net");
        harness.assertInGraveyard(player2, "Static Net");
    }

    @Test
    @DisplayName("Loran's enter-the-battlefield ability may choose no target")
    void entersWithoutTarget() {
        harness.setHand(player1, List.of(new LoranOfTheThirdPath()));
        addLoranMana();

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Loran of the Third Path");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Loran's ability makes each player draw a card")
    void eachPlayerDraws() {
        Permanent loran = addCreatureReady(player1, new LoranOfTheThirdPath());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new AirliftChaplain()));
        harness.setLibrary(player2, List.of(new EnergyRefractor()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(loran.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Airlift Chaplain");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Energy Refractor");
    }

    @Test
    @DisplayName("Loran's ability cannot target its controller")
    void abilityCannotTargetController() {
        addCreatureReady(player1, new LoranOfTheThirdPath());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Loran may destroy its controller's artifact")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new EnergyRefractor());
        UUID targetId = harness.getPermanentId(player1, "Energy Refractor");

        castLoran(List.of(targetId));

        harness.assertNotOnBattlefield(player1, "Energy Refractor");
        harness.assertInGraveyard(player1, "Energy Refractor");
    }

    @Test
    @DisplayName("Loran may choose no target even when an artifact is present")
    void leavesArtifactAloneWhenNoTargetChosen() {
        harness.addToBattlefield(player2, new EnergyRefractor());

        castLoran(List.of());

        harness.assertOnBattlefield(player2, "Energy Refractor");
        harness.assertOnBattlefield(player1, "Loran of the Third Path");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Loran cannot target a creature that is neither an artifact nor an enchantment")
    void cannotTargetOrdinaryCreature() {
        harness.addToBattlefield(player2, new AirliftChaplain());
        UUID targetId = harness.getPermanentId(player2, "Airlift Chaplain");
        harness.setHand(player1, List.of(new LoranOfTheThirdPath()));
        addLoranMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Summoning sickness prevents Loran's tap ability")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new LoranOfTheThirdPath());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Loran's activated ability resolves after Loran leaves the battlefield")
    void drawAbilityResolvesWithoutLoran() {
        Permanent loran = addCreatureReady(player1, new LoranOfTheThirdPath());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new AirliftChaplain()));
        harness.setLibrary(player2, List.of(new EnergyRefractor()));

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(loran);
        gd.playerGraveyards.get(player1.getId()).add(loran.getCard());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Airlift Chaplain");
        harness.assertInHand(player2, "Energy Refractor");
    }

    @Test
    @DisplayName("On the opponent's turn, the opponent draws before Loran's controller")
    void activeOpponentDrawsFirst() {
        addCreatureReady(player1, new LoranOfTheThirdPath());
        gd.activePlayerId = player2.getId();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new AirliftChaplain()));
        harness.setLibrary(player2, List.of(new EnergyRefractor()));
        gd.gameLog.clear();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(text -> text.endsWith(" draws a card.")).toList())
                .containsExactly(
                        gd.playerIdToName.get(player2.getId()) + " draws a card.",
                        gd.playerIdToName.get(player1.getId()) + " draws a card.");
    }

    @Test
    @DisplayName("Loran stays untapped when declared as an attacker")
    void vigilanceLeavesLoranUntapped() {
        Permanent loran = addCreatureReady(player1, new LoranOfTheThirdPath());

        declareAttackers(List.of(0));

        assertThat(loran.isAttacking()).isTrue();
        assertThat(loran.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Loran's draw ability requires an opponent target")
    void drawAbilityRequiresTarget() {
        addCreatureReady(player1, new LoranOfTheThirdPath());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target player");
    }

    @Test
    @DisplayName("A tapped Loran cannot activate its draw ability again")
    void tappedLoranCannotActivate() {
        Permanent loran = addCreatureReady(player1, new LoranOfTheThirdPath());
        loran.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private void castLoran(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new LoranOfTheThirdPath()));
        addLoranMana();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addLoranMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
