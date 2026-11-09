#!/usr/bin/env python3
"""Step 15, run 17z11c: the new card case and Favor of the Woods' target assertion.

Idempotent; optional repository-root argument (used for the HEAD worktree). Balduvian Warlord's reassigned blocker makes
an unblocked attacker blocked by an effect, which triggers the attacker's delayed "becomes blocked" ability (Barreling
Attack, CR 509.3c).
"""
import pathlib
import sys

root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else '.')
test = root / 'magical-vibes-card-test/b/src/test/java/com/github/laxika/magicalvibes/cards/b/BalduvianWarlordTest.java'
text = test.read_text()
if 'reassignedBlockerTriggersDelayedBecomesBlockedAbility' not in text:
    text = text.replace('import com.github.laxika.magicalvibes.model.Keyword;\n',
                        'import com.github.laxika.magicalvibes.model.Keyword;\n'
                        'import com.github.laxika.magicalvibes.model.ManaColor;\n')
    text = text.replace('@CardUsed({BalduvianWarlord.class, KjeldoranOutrider.class, TauntingElf.class})',
                        '@CardUsed({BalduvianWarlord.class, BarrelingAttack.class, KjeldoranOutrider.class, TauntingElf.class})')
    case = """    @Test
    @DisplayName("the attacker the reassigned blocker blocks gets its delayed becomes-blocked ability")
    void reassignedBlockerTriggersDelayedBecomesBlockedAbility() {
        Permanent warlord = addCreatureReady(player2, new BalduvianWarlord());
        Permanent formerAttacker = addCreatureReady(player1, new KjeldoranOutrider());
        Permanent chosenAttacker = addCreatureReady(player1, new KjeldoranOutrider());
        Permanent blocker = addCreatureReady(player2, new KjeldoranOutrider());
        harness.setHand(player1, List.of(new BarrelingAttack()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, chosenAttacker.getId());
        harness.passBothPriorities();

        setUpCombat(formerAttacker, chosenAttacker, blocker);
        assertThat(chosenAttacker.getPowerModifier()).isZero();

        harness.activateAbility(player2, indexOf(player2, warlord), null, blocker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, chosenAttacker.getId());

        // The attacker was unblocked, so becoming blocked by the reassigned creature triggers Barreling Attack's
        // "when that creature becomes blocked this turn" (CR 509.3c).
        assertThat(blocker.getBlockingTargetIds()).containsExactly(chosenAttacker.getId());
        harness.passBothPriorities();
        assertThat(chosenAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(chosenAttacker.getToughnessModifier()).isEqualTo(1);
    }

    private void setUpCombat(Permanent formerAttacker, Permanent chosenAttacker, Permanent blocker) {"""
    text = text.replace('    private void setUpCombat(Permanent formerAttacker, Permanent chosenAttacker, Permanent blocker) {', case, 1)
    test.write_text(text)
    print('added the Balduvian Warlord case')

# Favor of the Woods: the block entry records the blocked attacker as "that creature", its non-targeting target,
# whatever the effects are; the case asserted the old null target.
favor = root / 'magical-vibes-card-test/f/src/test/java/com/github/laxika/magicalvibes/cards/f/FavorOfTheWoodsTest.java'
text = favor.read_text()
old = """        // Not a targeted ability — targetId is null
        assertThat(entry.getTargetId()).isNull();
"""
new = """        // Not a targeted ability: the blocked attacker is recorded as "that creature", which is no target.
        assertThat(entry.isNonTargeting()).isTrue();
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
"""
if old in text:
    favor.write_text(text.replace(old, new))
    print('updated the Favor of the Woods case')
