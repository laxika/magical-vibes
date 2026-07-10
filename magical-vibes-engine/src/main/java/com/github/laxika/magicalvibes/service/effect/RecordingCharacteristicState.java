package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * A {@link CharacteristicState} copy that logs every mutation as a normalized operation
 * string, used by the CR 613.8 dependency trials in {@link LayerSystemService}: one effect's
 * fingerprint is the map of per-permanent operation lists it produces, and effect A depends on
 * effect B exactly when A's fingerprint differs between the world with and without B applied.
 *
 * <p>The operations are deliberately <em>what the effect attempts to do</em>, not the concrete
 * state change: {@code loseAllAbilities} logs one constant token regardless of which abilities
 * happen to be present, and removal predicates are logged opaquely. This matches the official
 * CR 613.8 doctrine (a "loses all abilities" effect is NOT dependent on an earlier grant just
 * because it would concretely remove more — Humility ordering is timestamp-based), while
 * changes to the affected set or to the effect's existence show up as operations appearing on
 * different permanents or disappearing entirely.
 */
final class RecordingCharacteristicState extends CharacteristicState {

    private final List<String> ops = new ArrayList<>();
    private boolean recording;

    RecordingCharacteristicState(CharacteristicState source) {
        super(source);
    }

    void startRecording() {
        recording = true;
    }

    List<String> ops() {
        return ops;
    }

    private void log(String op) {
        if (recording) {
            ops.add(op);
        }
    }

    private static String sorted(Collection<? extends Enum<?>> values) {
        return values.stream().map(Enum::name).sorted().toList().toString();
    }

    @Override
    public void setName(String name) {
        log("setName:" + name);
        super.setName(name);
    }

    @Override
    public void addCardType(CardType type) {
        log("addCardType:" + type);
        super.addCardType(type);
    }

    @Override
    public void removeCardType(CardType type) {
        log("removeCardType:" + type);
        super.removeCardType(type);
    }

    @Override
    public void addSupertype(CardSupertype supertype) {
        log("addSupertype:" + supertype);
        super.addSupertype(supertype);
    }

    @Override
    public void addSubtype(CardSubtype subtype) {
        log("addSubtype:" + subtype);
        super.addSubtype(subtype);
    }

    @Override
    public void overrideSubtypes(Collection<CardSubtype> replacement) {
        log("overrideSubtypes:" + sorted(replacement));
        super.overrideSubtypes(replacement);
    }

    @Override
    public void removeSubtypesIf(Predicate<CardSubtype> filter) {
        // Opaque by design: the removal is "what the effect attempts", independent of which
        // subtypes are concretely present.
        log("removeSubtypesIf");
        super.removeSubtypesIf(filter);
    }

    @Override
    public void addColor(CardColor color) {
        log("addColor:" + color);
        super.addColor(color);
    }

    @Override
    public void overrideColors(Collection<CardColor> replacement) {
        log("overrideColors:" + sorted(replacement));
        super.overrideColors(replacement);
    }

    @Override
    public void addKeyword(Keyword keyword) {
        log("addKeyword:" + keyword);
        super.addKeyword(keyword);
    }

    @Override
    public void addKeywords(Collection<Keyword> granted) {
        log("addKeywords:" + sorted(granted));
        super.addKeywords(granted);
    }

    @Override
    public void removeKeyword(Keyword keyword) {
        log("removeKeyword:" + keyword);
        super.removeKeyword(keyword);
    }

    @Override
    public void addProtectionColors(Collection<CardColor> colors) {
        log("addProtectionColors:" + sorted(colors));
        super.addProtectionColors(colors);
    }

    @Override
    public void addActivatedAbility(ActivatedAbility ability) {
        // Granted abilities are shared instances; identity is stable across the trials of one
        // dependency computation.
        log("addActivatedAbility:" + System.identityHashCode(ability));
        super.addActivatedAbility(ability);
    }

    @Override
    public void addStaticEffect(CardEffect effect) {
        log("addStaticEffect:" + System.identityHashCode(effect));
        super.addStaticEffect(effect);
    }

    @Override
    public void loseAllAbilities(long timestamp) {
        // No timestamp in the token: the effect always attempts the same removal.
        log("loseAllAbilities");
        super.loseAllAbilities(timestamp);
    }

    @Override
    public void removePrintedAbilities() {
        log("removePrintedAbilities");
        super.removePrintedAbilities();
    }

    @Override
    public void addPowerToughnessDelta(int power, int toughness) {
        log("addPowerToughnessDelta:" + power + "/" + toughness);
        super.addPowerToughnessDelta(power, toughness);
    }

    @Override
    public void switchPowerToughness() {
        log("switchPowerToughness");
        super.switchPowerToughness();
    }
}
