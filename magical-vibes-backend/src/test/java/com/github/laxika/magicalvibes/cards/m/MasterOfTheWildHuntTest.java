package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PackHuntEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MasterOfTheWildHuntTest extends BaseCardTest {

    // ===== Card setup =====

    @Test
    @DisplayName("Has upkeep token trigger and tap activated ability")
    void hasCorrectAbilities() {
        MasterOfTheWildHunt card = new MasterOfTheWildHunt();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(CreateTokenEffect.class);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(PackHuntEffect.class);
    }

    // ===== Upkeep trigger: token creation =====

    @Test
    @DisplayName("Creates a 2/2 green Wolf token at beginning of controller's upkeep")
    void createsWolfTokenOnUpkeep() {
        addReadyMaster(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = getTokens(player1);
        assertThat(tokens).hasSize(1);

        Permanent wolf = tokens.getFirst();
        assertThat(wolf.getCard().getName()).isEqualTo("Wolf");
        assertThat(wolf.getCard().getPower()).isEqualTo(2);
        assertThat(wolf.getCard().getToughness()).isEqualTo(2);
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(wolf.getCard().getType()).isEqualTo(CardType.CREATURE);
    }

    @Test
    @DisplayName("Accumulates Wolf tokens over multiple upkeeps")
    void accumulatesTokensOverMultipleUpkeeps() {
        addReadyMaster(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(getTokens(player1)).hasSize(2);
    }

    // ===== Activated ability: basic pack hunt =====

    @Test
    @DisplayName("Wolves deal damage equal to their power to target creature")
    void wolvesDealDamageToTarget() {
        Permanent master = addReadyMaster(player1);
        Permanent wolf1 = addReadyWolf(player1);
        Permanent wolf2 = addReadyWolf(player1);

        // Opponent has a 2/2
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities(); // resolve ability

        // Two 2/2 wolves deal 4 total damage to the 2/2 — lethal
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Taps all untapped Wolf creatures when ability resolves")
    void tapsAllWolves() {
        addReadyMaster(player1);
        Permanent wolf1 = addReadyWolf(player1);
        Permanent wolf2 = addReadyWolf(player1);

        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(wolf1.isTapped()).isTrue();
        assertThat(wolf2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Master itself gets tapped as part of the activation cost")
    void masterTappedAsCost() {
        Permanent master = addReadyMaster(player1);
        addReadyWolf(player1);

        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(master.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Target creature deals damage back divided evenly among wolves")
    void targetDealsDamageBackToWolves() {
        addReadyMaster(player1);
        Permanent wolf1 = addReadyWolf(player1);
        Permanent wolf2 = addReadyWolf(player1);

        // Opponent's 2/2 deals 2 damage divided among 2 wolves = 1 each
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Each wolf takes 1 damage (2/2 = 1 per wolf), both survive
        assertThat(wolf1.getMarkedDamage()).isEqualTo(1);
        assertThat(wolf2.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Target creature with high power can kill wolves with damage back")
    void highPowerTargetKillsWolves() {
        addReadyMaster(player1);
        Permanent wolf1 = addReadyWolf(player1);
        Permanent wolf2 = addReadyWolf(player1);

        // Opponent's creature with 5 power, 5 toughness
        Permanent target = addReadyCreatureWithStats(player2, "Big Creature", 5, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Two 2/2 wolves deal 4 total to 5/5 — not lethal, target survives with 4 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.getMarkedDamage()).isEqualTo(4);

        // 5 damage divided among 2 wolves = 3 and 2 — both wolves die (2 toughness)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(wolf1.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(wolf2.getId()));
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Already-tapped wolves are not tapped again and don't deal damage")
    void alreadyTappedWolvesSkipped() {
        addReadyMaster(player1);
        Permanent wolf1 = addReadyWolf(player1);
        Permanent wolf2 = addReadyWolf(player1);
        wolf2.tap(); // already tapped

        // 1/4 target to survive and measure damage
        Permanent target = addReadyCreatureWithStats(player2, "Tough Creature", 1, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Only wolf1 (untapped) deals 2 damage; wolf2 was already tapped
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(wolf1.isTapped()).isTrue();
    }

    @Test
    @DisplayName("No wolves means no damage is dealt in either direction")
    void noWolvesNoDamage() {
        addReadyMaster(player1);
        // No wolves on the battlefield

        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Target should be unharmed
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Wolves with 0 power deal no damage to target")
    void zeroPowerWolvesNoDamage() {
        addReadyMaster(player1);
        Permanent wolf = addReadyCreatureWithSubtype(player1, "Weak Wolf", 0, 2, CardSubtype.WOLF);

        Permanent target = addReadyCreatureWithStats(player2, "Target", 1, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Wolf has 0 power, deals no damage
        assertThat(target.getMarkedDamage()).isZero();
        // Target has 1 power divided among 1 wolf = 1 damage to the wolf
        assertThat(wolf.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Target creature with 0 power deals no damage back to wolves")
    void zeroPowerTargetNoDamageBack() {
        addReadyMaster(player1);
        Permanent wolf = addReadyWolf(player1);

        Permanent target = addReadyCreatureWithStats(player2, "Wall", 0, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Wolf deals 2 to target
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        // 0 power target deals no damage back
        assertThat(wolf.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Only Wolf creatures are tapped, not other creature types")
    void onlyWolvesTapped() {
        addReadyMaster(player1);
        Permanent wolf = addReadyWolf(player1);
        Permanent nonWolf = addReadyCreature(player1, new GrizzlyBears()); // Bear, not Wolf

        Permanent target = addReadyCreatureWithStats(player2, "Target", 1, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(wolf.isTapped()).isTrue();
        assertThat(nonWolf.isTapped()).isFalse();
        // Only wolf's 2 power was dealt
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not tap opponent's Wolf creatures")
    void doesNotTapOpponentWolves() {
        addReadyMaster(player1);
        Permanent myWolf = addReadyWolf(player1);
        Permanent opponentWolf = addReadyWolf(player2);

        Permanent target = addReadyCreatureWithStats(player2, "Target", 0, 10);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(myWolf.isTapped()).isTrue();
        assertThat(opponentWolf.isTapped()).isFalse();
        // Only my wolf dealt damage
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addReadyMaster(Player player) {
        Permanent perm = new Permanent(new MasterOfTheWildHunt());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyWolf(Player player) {
        return addReadyCreatureWithSubtype(player, "Wolf", 2, 2, CardSubtype.WOLF);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreatureWithStats(Player player, String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setManaCost("");
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreatureWithSubtype(Player player, String name, int power, int toughness, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setManaCost("");
        card.setSubtypes(List.of(subtype));
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private List<Permanent> getTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
    }
}
