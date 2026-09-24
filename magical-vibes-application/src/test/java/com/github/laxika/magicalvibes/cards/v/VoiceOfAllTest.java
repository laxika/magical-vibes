package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SilverDrake;
import com.github.laxika.magicalvibes.cards.s.Singe;
import com.github.laxika.magicalvibes.cards.s.SinisterStrength;
import com.github.laxika.magicalvibes.cards.s.StoneKavu;
import com.github.laxika.magicalvibes.cards.t.TahngarthTalruumHero;
import com.github.laxika.magicalvibes.cards.v.VolcanoImp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoiceOfAll.class, TahngarthTalruumHero.class, StoneKavu.class, VolcanoImp.class,
        SilverDrake.class, Singe.class, SinisterStrength.class})
class VoiceOfAllTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Voice of All puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Cannot cast Voice of All without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving triggers color choice =====

    @Test
    @DisplayName("Resolving Voice of All enters battlefield and awaits color choice")
    void resolvingTriggersColorChoice() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Voice of All");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color sets chosenColor on the permanent")
    void choosingColorSetsOnPermanent() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent perm = findPermanent(player1, "Voice of All");
        assertThat(perm.getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Chosen-color protection is included in the battlefield view")
    void chosenColorProtectionIsIncludedInBattlefieldView() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining(
                "\"grantedAbilities\":[{\"text\":\"Protection from red\",\"sourceName\":\"Voice of All\"}]"))
                .isNotEmpty();
    }

    @Test
    @DisplayName("Color choice clears awaiting state")
    void colorChoiceClearsAwaitingState() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Color choice is logged")
    void colorChoiceIsLogged() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("chooses black") && log.contains("Voice of All"));
    }

    @Test
    @DisplayName("Voice of All enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        Permanent perm = findPermanent(player1, "Voice of All");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Voice of All has flying on the battlefield")
    void hasFlyingOnBattlefield() {
        harness.addToBattlefield(player1, new VoiceOfAll());

        Permanent perm = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, perm, Keyword.FLYING)).isTrue();
    }

    // ===== Color choice validation =====

    @Test
    @DisplayName("Wrong player cannot choose color")
    void wrongPlayerCannotChooseColor() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player2, "RED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn");
    }

    @Test
    @DisplayName("Cannot choose color when not awaiting color choice")
    void cannotChooseColorWhenNotAwaiting() {
        assertThatThrownBy(() -> harness.handleListChoice(player1, "RED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not awaiting");
    }

    // ===== Protection - combat damage =====

    @Test
    @DisplayName("Voice of All takes no combat damage from chosen color creature")
    void takesNoDamageFromChosenColor() {
        Permanent voiceOfAll = addCreatureReady(player2, new VoiceOfAll());
        voiceOfAll.setChosenColor(CardColor.RED);
        voiceOfAll.setBlocking(true);
        voiceOfAll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new TahngarthTalruumHero());
        attacker.setAttacking(true);
        resolveCombat();

        // Voice of All survives — red damage prevented (protection from red)
        harness.assertOnBattlefield(player2, "Voice of All");
        // Tahngarth takes 2 from Voice of All (2 < 4 toughness) → survives
        harness.assertOnBattlefield(player1, "Tahngarth, Talruum Hero");
    }

    @Test
    @DisplayName("Voice of All takes normal combat damage from non-chosen color creature")
    void takesNormalDamageFromNonChosenColor() {
        Permanent voiceOfAll = addCreatureReady(player2, new VoiceOfAll());
        voiceOfAll.setChosenColor(CardColor.RED);
        voiceOfAll.setBlocking(true);
        voiceOfAll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new StoneKavu());
        attacker.setAttacking(true);
        resolveCombat();

        // Voice of All dies — green is not the chosen color, 3 >= 2 toughness
        harness.assertNotOnBattlefield(player2, "Voice of All");
        harness.assertInGraveyard(player2, "Voice of All");
    }

    // ===== Protection - blocking =====

    @Test
    @DisplayName("Chosen color flying creature cannot block Voice of All")
    void chosenColorCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new VoiceOfAll());
        attacker.setChosenColor(CardColor.BLACK);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new VolcanoImp());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-chosen color flying creature can block Voice of All")
    void nonChosenColorCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new VoiceOfAll());
        attacker.setChosenColor(CardColor.BLACK);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SilverDrake());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    // ===== Protection - targeting =====

    @Test
    @DisplayName("Cannot be targeted by instant of chosen color")
    void cannotBeTargetedByChosenColorInstant() {
        Permanent voiceOfAll = addCreatureReady(player2, new VoiceOfAll());
        voiceOfAll.setChosenColor(CardColor.RED);

        // Add valid target so spell is playable
        addCreatureReady(player2, new StoneKavu());

        harness.setHand(player1, List.of(new Singe()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, voiceOfAll.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Can be targeted by instant of non-chosen color")
    void canBeTargetedByNonChosenColorInstant() {
        Permanent voiceOfAll = addCreatureReady(player1, new VoiceOfAll());
        voiceOfAll.setChosenColor(CardColor.BLACK);

        harness.setHand(player1, List.of(new Singe()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, voiceOfAll.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(voiceOfAll.getId());
    }

    // ===== Protection - aura enchantment =====

    @Test
    @DisplayName("Cannot be enchanted by aura of chosen color")
    void cannotBeEnchantedByChosenColorAura() {
        Permanent voiceOfAll = addCreatureReady(player2, new VoiceOfAll());
        voiceOfAll.setChosenColor(CardColor.BLACK);

        // Add valid target so aura is playable
        addCreatureReady(player2, new StoneKavu());

        harness.setHand(player1, List.of(new SinisterStrength()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, voiceOfAll.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    // ===== Different color choices =====

    @Test
    @DisplayName("Can choose each of the five colors")
    void canChooseEachColor() {
        for (String colorName : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            GameTestHarness h = new GameTestHarness();
            Player p1 = h.getPlayer1();
            h.skipMulligan();

            h.setHand(p1, List.of(new VoiceOfAll()));
            h.addMana(p1, ManaColor.WHITE, 4);

            h.castCreature(p1, 0);
            h.passBothPriorities();
            h.handleListChoice(p1, colorName);

            GameData data = h.getGameData();
            Permanent perm = data.playerBattlefields.get(p1.getId()).getFirst();
            assertThat(perm.getChosenColor()).isEqualTo(CardColor.valueOf(colorName));
        }
    }

    // ===== Protection from chosen white =====

    @Test
    @DisplayName("Choosing white grants protection from white creatures in combat")
    void protectionFromWhiteInCombat() {
        Permanent voiceOfAll = addCreatureReady(player2, new VoiceOfAll());
        voiceOfAll.setChosenColor(CardColor.WHITE);
        voiceOfAll.setBlocking(true);
        voiceOfAll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new SilverDrake());
        attacker.setAttacking(true);
        resolveCombat();

        // Voice of All survives — damage from the white-blue source is prevented
        harness.assertOnBattlefield(player2, "Voice of All");
    }

    // ===== No protection without color choice =====

    @Test
    @DisplayName("Without choosing a color, Voice of All has no protection")
    void noProtectionWithoutColorChoice() {
        Permanent voiceOfAll = addCreatureReady(player2, new VoiceOfAll());
        // No chosenColor set
        voiceOfAll.setBlocking(true);
        voiceOfAll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new TahngarthTalruumHero());
        attacker.setAttacking(true);
        resolveCombat();

        // Voice of All dies — no protection without choosing a color (4 >= 2)
        harness.assertNotOnBattlefield(player2, "Voice of All");
        harness.assertInGraveyard(player2, "Voice of All");
    }
}

