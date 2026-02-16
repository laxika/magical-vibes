package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromChosenColorEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoiceOfAllTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createFlyingCreature(String name, int power, int toughness, CardColor color) {
        Card card = createCreature(name, power, toughness, color);
        card.setKeywords(Set.of(Keyword.FLYING));
        return card;
    }

    private static Card createTargetedInstant(String name, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setNeedsTarget(true);
        return card;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Voice of All has correct card properties")
    void hasCorrectProperties() {
        VoiceOfAll card = new VoiceOfAll();

        assertThat(card.getName()).isEqualTo("Voice of All");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{W}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getKeywords()).containsExactly(Keyword.FLYING);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.ANGEL);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ProtectionFromChosenColorEffect.class);
    }

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
        assertThat(entry.getCard().getName()).isEqualTo("Voice of All");
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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Voice of All"));
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.awaitingColorChoicePlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color sets chosenColor on the permanent")
    void choosingColorSetsOnPermanent() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleColorChosen(player1, "RED");

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Voice of All"))
                .findFirst().orElseThrow();
        assertThat(perm.getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Color choice clears awaiting state")
    void colorChoiceClearsAwaitingState() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleColorChosen(player1, "BLUE");

        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.awaitingColorChoicePlayerId).isNull();
        assertThat(gd.awaitingColorChoicePermanentId).isNull();
    }

    @Test
    @DisplayName("Color choice is logged")
    void colorChoiceIsLogged() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleColorChosen(player1, "BLACK");

        assertThat(gd.gameLog).anyMatch(log -> log.contains("chooses black") && log.contains("Voice of All"));
    }

    @Test
    @DisplayName("Voice of All enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleColorChosen(player1, "GREEN");

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Voice of All"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Voice of All has flying on the battlefield")
    void hasFlyingOnBattlefield() {
        harness.addToBattlefield(player1, new VoiceOfAll());

        Permanent perm = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(perm.hasKeyword(Keyword.FLYING)).isTrue();
    }

    // ===== Color choice validation =====

    @Test
    @DisplayName("Wrong player cannot choose color")
    void wrongPlayerCannotChooseColor() {
        harness.setHand(player1, List.of(new VoiceOfAll()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleColorChosen(player2, "RED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn");
    }

    @Test
    @DisplayName("Cannot choose color when not awaiting color choice")
    void cannotChooseColorWhenNotAwaiting() {
        assertThatThrownBy(() -> harness.handleColorChosen(player1, "RED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not awaiting color choice");
    }

    // ===== Protection - combat damage =====

    @Test
    @DisplayName("Voice of All takes no combat damage from chosen color creature")
    void takesNoDamageFromChosenColor() {
        Permanent voiceOfAll = new Permanent(new VoiceOfAll());
        voiceOfAll.setSummoningSick(false);
        voiceOfAll.setChosenColor(CardColor.RED);
        voiceOfAll.setBlocking(true);
        voiceOfAll.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(voiceOfAll);

        Permanent attacker = new Permanent(createCreature("Fire Elemental", 5, 4, CardColor.RED));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Voice of All survives — red damage prevented (protection from red)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Voice of All"));
        // Fire Elemental takes 2 from Voice of All (2 < 4 toughness) → survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Fire Elemental"));
    }

    @Test
    @DisplayName("Voice of All takes normal combat damage from non-chosen color creature")
    void takesNormalDamageFromNonChosenColor() {
        Permanent voiceOfAll = new Permanent(new VoiceOfAll());
        voiceOfAll.setSummoningSick(false);
        voiceOfAll.setChosenColor(CardColor.RED);
        voiceOfAll.setBlocking(true);
        voiceOfAll.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(voiceOfAll);

        Permanent attacker = new Permanent(createCreature("Big Green", 3, 3, CardColor.GREEN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Voice of All dies — green is not the chosen color, 3 >= 2 toughness
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Voice of All"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Voice of All"));
    }

    // ===== Protection - blocking =====

    @Test
    @DisplayName("Chosen color flying creature cannot block Voice of All")
    void chosenColorCreatureCannotBlock() {
        Permanent attacker = new Permanent(new VoiceOfAll());
        attacker.setSummoningSick(false);
        attacker.setChosenColor(CardColor.BLACK);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createFlyingCreature("Black Dragon", 2, 2, CardColor.BLACK));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-chosen color flying creature can block Voice of All")
    void nonChosenColorCreatureCanBlock() {
        Permanent attacker = new Permanent(new VoiceOfAll());
        attacker.setSummoningSick(false);
        attacker.setChosenColor(CardColor.BLACK);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createFlyingCreature("Green Dragon", 2, 2, CardColor.GREEN));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    // ===== Protection - targeting =====

    @Test
    @DisplayName("Cannot be targeted by instant of chosen color")
    void cannotBeTargetedByChosenColorInstant() {
        Permanent voiceOfAll = new Permanent(new VoiceOfAll());
        voiceOfAll.setSummoningSick(false);
        voiceOfAll.setChosenColor(CardColor.BLACK);
        gd.playerBattlefields.get(player2.getId()).add(voiceOfAll);

        harness.setHand(player1, List.of(createTargetedInstant("Dark Banishing", CardColor.BLACK, "{B}")));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, voiceOfAll.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by instant of non-chosen color")
    void canBeTargetedByNonChosenColorInstant() {
        Permanent voiceOfAll = new Permanent(new VoiceOfAll());
        voiceOfAll.setSummoningSick(false);
        voiceOfAll.setChosenColor(CardColor.BLACK);
        gd.playerBattlefields.get(player1.getId()).add(voiceOfAll);

        harness.setHand(player1, List.of(createTargetedInstant("Lightning Bolt", CardColor.RED, "{R}")));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, voiceOfAll.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lightning Bolt");
    }

    // ===== Protection - aura enchantment =====

    @Test
    @DisplayName("Cannot be enchanted by aura of chosen color")
    void cannotBeEnchantedByChosenColorAura() {
        Permanent voiceOfAll = new Permanent(new VoiceOfAll());
        voiceOfAll.setSummoningSick(false);
        voiceOfAll.setChosenColor(CardColor.BLACK);
        gd.playerBattlefields.get(player2.getId()).add(voiceOfAll);

        Card blackAura = new Card();
        blackAura.setName("Unholy Strength");
        blackAura.setType(CardType.ENCHANTMENT);
        blackAura.setManaCost("{B}");
        blackAura.setColor(CardColor.BLACK);
        blackAura.setSubtypes(List.of(CardSubtype.AURA));
        blackAura.setNeedsTarget(true);
        harness.setHand(player1, List.of(blackAura));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, voiceOfAll.getId(), null))
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
            h.handleColorChosen(p1, colorName);

            GameData data = h.getGameData();
            Permanent perm = data.playerBattlefields.get(p1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Voice of All"))
                    .findFirst().orElseThrow();
            assertThat(perm.getChosenColor()).isEqualTo(CardColor.valueOf(colorName));
        }
    }

    // ===== Protection from chosen white =====

    @Test
    @DisplayName("Choosing white grants protection from white creatures in combat")
    void protectionFromWhiteInCombat() {
        Permanent voiceOfAll = new Permanent(new VoiceOfAll());
        voiceOfAll.setSummoningSick(false);
        voiceOfAll.setChosenColor(CardColor.WHITE);
        voiceOfAll.setBlocking(true);
        voiceOfAll.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(voiceOfAll);

        Permanent attacker = new Permanent(createCreature("White Knight", 3, 3, CardColor.WHITE));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Voice of All survives — white damage prevented
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Voice of All"));
    }

    // ===== No protection without color choice =====

    @Test
    @DisplayName("Without choosing a color, Voice of All has no protection")
    void noProtectionWithoutColorChoice() {
        Permanent voiceOfAll = new Permanent(new VoiceOfAll());
        voiceOfAll.setSummoningSick(false);
        // No chosenColor set
        voiceOfAll.setBlocking(true);
        voiceOfAll.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(voiceOfAll);

        Permanent attacker = new Permanent(createCreature("Big Red", 3, 3, CardColor.RED));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Voice of All dies — no protection without choosing a color (3 >= 2)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Voice of All"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Voice of All"));
    }
}
