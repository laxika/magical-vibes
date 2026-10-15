package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.w.WallOfFrost;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterOfTheWildHunt.class, RuneclawBear.class, CrawWurm.class, WallOfFrost.class})
class MasterOfTheWildHuntTest extends BaseCardTest {

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

    @Test
    @DisplayName("Wolves deal damage equal to their power to target creature")
    void wolvesDealDamageToTarget() {
        addReadyMaster(player1);
        addReadyWolf(player1);
        addReadyWolf(player1);

        // Opponent has a 2/2
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities(); // resolve ability

        // Two 2/2 wolves deal 4 total damage to the 2/2 — lethal
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Taps all untapped Wolf creatures when ability resolves")
    void tapsAllWolves() {
        addReadyMaster(player1);
        Permanent wolf1 = addReadyWolf(player1);
        Permanent wolf2 = addReadyWolf(player1);

        Permanent target = addCreatureReady(player2, new RuneclawBear());

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

        Permanent target = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(master.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A lethally damaged target still deals damage back to the sole Wolf")
    void lethallyDamagedTargetDealsDamageBack() {
        addReadyMaster(player1);
        Permanent wolf = addReadyWolf(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(wolf.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wolf);
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("A high-power target kills the sole Wolf and survives its damage")
    void highPowerTargetKillsWolf() {
        addReadyMaster(player1);
        Permanent wolf = addReadyWolf(player1);
        Permanent target = addCreatureReady(player2, new CrawWurm());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wolf);
    }

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

        Permanent target = addCreatureReady(player2, new RuneclawBear());

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
        Permanent nonWolf = addCreatureReady(player1, new RuneclawBear()); // Bear, not Wolf

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

    @Test
    @DisplayName("The target controller must choose how to distribute return damage")
    void targetControllerChoosesReturnDamage() {
        addReadyMaster(player1);
        addReadyWolf(player1);
        addReadyWolf(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput())
                .as("The target controller can put both damage on one Wolf instead of splitting it")
                .isTrue();
    }

    @Test
    @DisplayName("A creature that gained Wolf subtype participates in the hunt")
    void creatureWithGrantedWolfSubtypeParticipates() {
        addReadyMaster(player1);
        Permanent wolf = addCreatureReady(player1, new RuneclawBear());
        wolf.getTransientSubtypes().add(CardSubtype.WOLF);
        Permanent target = addCreatureReady(player2, new WallOfFrost());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(wolf.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("An illegal target prevents the Wolves from being tapped")
    void illegalTargetDoesNotTapWolves() {
        addReadyMaster(player1);
        Permanent wolf = addReadyWolf(player1);
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(wolf.isTapped()).isFalse();
        assertThat(wolf.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The upkeep ability does not trigger on the opponent's upkeep")
    void noTokenOnOpponentUpkeep() {
        addReadyMaster(player1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(getTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Summoning-sick Wolves can be tapped by the resolving ability")
    void summoningSickWolfParticipates() {
        addReadyMaster(player1);
        Permanent wolf = addReadyWolf(player1);
        wolf.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new WallOfFrost());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(wolf.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addReadyMaster(Player player) {
        return addCreatureReady(player, new MasterOfTheWildHunt());
    }

    private Permanent addReadyWolf(Player player) {
        return addReadyCreatureWithSubtype(player, "Wolf", 2, 2, CardSubtype.WOLF);
    }

    private Permanent addReadyCreatureWithStats(Player player, String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setManaCost("");
        return addCreatureReady(player, card);
    }

    private Permanent addReadyCreatureWithSubtype(Player player, String name, int power, int toughness, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setManaCost("");
        card.setSubtypes(List.of(subtype));
        return addCreatureReady(player, card);
    }

    private List<Permanent> getTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
    }
}
