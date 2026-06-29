package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SlayerOfTheWickedTest extends BaseCardTest {

    private static Card createCreatureWithSubtype(String name, int power, int toughness,
                                                  CardColor color, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    /**
     * Casts Slayer of the Wicked and resolves it onto the battlefield, then accepts the may ability
     * and chooses the target so the ETB triggered ability is placed on the stack.
     */
    private void castAndAcceptMay(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SlayerOfTheWicked()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice prompt
        harness.handlePermanentChosen(player1, targetId); // choose target -> ETB on stack
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Slayer of the Wicked has correct ETB may destroy effect")
    void hasCorrectProperties() {
        SlayerOfTheWicked card = new SlayerOfTheWicked();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(DestroyTargetPermanentEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Slayer of the Wicked puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SlayerOfTheWicked()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Slayer of the Wicked");
    }

    @Test
    @DisplayName("Resolving puts Slayer of the Wicked on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new SlayerOfTheWicked()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Slayer of the Wicked"));
    }

    // ===== ETB may ability =====

    @Test
    @DisplayName("Resolving Slayer triggers may ability prompt when valid target exists")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new ScatheZombies());
        harness.setHand(player1, List.of(new SlayerOfTheWicked()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may ability prompts for target selection")
    void acceptingMayPromptsForTarget() {
        harness.addToBattlefield(player2, new ScatheZombies());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SlayerOfTheWicked()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    // ===== Destroys each valid subtype =====

    @Test
    @DisplayName("ETB destroys target Zombie")
    void etbDestroysTargetZombie() {
        harness.addToBattlefield(player2, new ScatheZombies());
        UUID zombieId = harness.getPermanentId(player2, "Scathe Zombies");
        castAndAcceptMay(zombieId);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Scathe Zombies"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Scathe Zombies"));
    }

    @Test
    @DisplayName("ETB destroys target Vampire")
    void etbDestroysTargetVampire() {
        Card vampire = createCreatureWithSubtype("Vampire Interloper", 2, 1, CardColor.BLACK, CardSubtype.VAMPIRE);
        harness.addToBattlefield(player2, vampire);
        UUID vampireId = harness.getPermanentId(player2, "Vampire Interloper");
        castAndAcceptMay(vampireId);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Vampire Interloper"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Vampire Interloper"));
    }

    @Test
    @DisplayName("ETB destroys target Werewolf")
    void etbDestroysTargetWerewolf() {
        Card werewolf = createCreatureWithSubtype("Reckless Waif", 3, 2, CardColor.RED, CardSubtype.WEREWOLF);
        harness.addToBattlefield(player2, werewolf);
        UUID werewolfId = harness.getPermanentId(player2, "Reckless Waif");
        castAndAcceptMay(werewolfId);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Reckless Waif"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Reckless Waif"));
    }

    // ===== Declining may =====

    @Test
    @DisplayName("Declining may ability does not destroy target")
    void decliningMaySkipsDestruction() {
        harness.addToBattlefield(player2, new ScatheZombies());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SlayerOfTheWicked()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Slayer of the Wicked"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Scathe Zombies"));
    }

    // ===== No valid targets =====

    @Test
    @DisplayName("May prompt still fires when no valid targets on battlefield")
    void mayPromptFiresWithoutValidTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlayerOfTheWicked()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may with no valid targets results in no valid targets")
    void acceptingMayWithNoValidTargetsHasNoEffect() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SlayerOfTheWicked()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> no targets

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid targets"));
    }

    // ===== Can target own creatures =====

    @Test
    @DisplayName("Can target own Zombie creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new ScatheZombies());
        UUID zombieId = harness.getPermanentId(player1, "Scathe Zombies");
        castAndAcceptMay(zombieId);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Scathe Zombies"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scathe Zombies"));
    }

    // ===== Slayer remains on battlefield =====

    @Test
    @DisplayName("Slayer of the Wicked remains on battlefield after destroying target")
    void slayerRemainsOnBattlefield() {
        harness.addToBattlefield(player2, new ScatheZombies());
        UUID zombieId = harness.getPermanentId(player2, "Scathe Zombies");
        castAndAcceptMay(zombieId);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Slayer of the Wicked"));
    }

    // ===== Stack is empty after full resolution =====

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterFullResolution() {
        harness.addToBattlefield(player2, new ScatheZombies());
        UUID zombieId = harness.getPermanentId(player2, "Scathe Zombies");
        castAndAcceptMay(zombieId);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
