package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.service.effect.DependencyFootprint.Characteristic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
 * different permanents or disappearing entirely. Every operation string is built from the
 * method's arguments only, never from the state, so an effect's operations change only when what
 * it read changes.</p>
 *
 * <p>In a base trial the copy also records every read and write into the trial's
 * {@link DependencyFootprint}, under the {@link Characteristic} the accessed field belongs to.
 * Every public method of {@link CharacteristicState} is overridden here, the Lombok getters and
 * setters included, so no access goes unrecorded; {@code RecordingCharacteristicStateTest} keeps
 * it that way.</p>
 */
final class RecordingCharacteristicState extends CharacteristicState {

    private final List<String> ops = new ArrayList<>();
    private final UUID permanentId;
    private final DependencyFootprint footprint;
    private boolean recording;

    RecordingCharacteristicState(CharacteristicState source, UUID permanentId, DependencyFootprint footprint) {
        super(source);
        this.permanentId = permanentId;
        this.footprint = footprint;
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

    private void read(Characteristic characteristic) {
        if (recording && footprint != null) {
            footprint.read(characteristic, permanentId);
        }
    }

    private void write(Characteristic characteristic) {
        if (recording && footprint != null) {
            footprint.write(characteristic, permanentId);
        }
    }

    private static String sorted(Collection<? extends Enum<?>> values) {
        return values.stream().map(Enum::name).sorted().toList().toString();
    }

    @Override
    public String getName() {
        read(Characteristic.NAME);
        return super.getName();
    }

    @Override
    public void setName(String name) {
        log("setName:" + name);
        write(Characteristic.NAME);
        super.setName(name);
    }

    @Override
    public Set<CardType> getCardTypes() {
        read(Characteristic.TYPES);
        return super.getCardTypes();
    }

    @Override
    public boolean isCardTypesOverridden() {
        read(Characteristic.TYPES);
        return super.isCardTypesOverridden();
    }

    @Override
    public Set<CardSupertype> getSupertypes() {
        read(Characteristic.TYPES);
        return super.getSupertypes();
    }

    @Override
    public List<CardSubtype> getSubtypes() {
        read(Characteristic.TYPES);
        return super.getSubtypes();
    }

    @Override
    public boolean hasCardType(CardType type) {
        read(Characteristic.TYPES);
        return super.hasCardType(type);
    }

    @Override
    public boolean hasSubtype(CardSubtype subtype) {
        read(Characteristic.TYPES);
        return super.hasSubtype(subtype);
    }

    @Override
    public boolean hasSupertype(CardSupertype supertype) {
        read(Characteristic.TYPES);
        return super.hasSupertype(supertype);
    }

    @Override
    public void addCardType(CardType type) {
        log("addCardType:" + type);
        write(Characteristic.TYPES);
        super.addCardType(type);
    }

    @Override
    public void removeCardType(CardType type) {
        log("removeCardType:" + type);
        write(Characteristic.TYPES);
        super.removeCardType(type);
    }

    @Override
    public void overrideCardTypes(Collection<CardType> replacement) {
        log("overrideCardTypes:" + sorted(replacement));
        write(Characteristic.TYPES);
        super.overrideCardTypes(replacement);
    }

    @Override
    public void addSupertype(CardSupertype supertype) {
        log("addSupertype:" + supertype);
        write(Characteristic.TYPES);
        super.addSupertype(supertype);
    }

    @Override
    public void removeSupertype(CardSupertype supertype) {
        log("removeSupertype:" + supertype);
        write(Characteristic.TYPES);
        super.removeSupertype(supertype);
    }

    @Override
    public void addSubtype(CardSubtype subtype) {
        log("addSubtype:" + subtype);
        write(Characteristic.TYPES);
        super.addSubtype(subtype);
    }

    @Override
    public void overrideSubtypes(Collection<CardSubtype> replacement) {
        log("overrideSubtypes:" + sorted(replacement));
        write(Characteristic.TYPES);
        super.overrideSubtypes(replacement);
    }

    @Override
    public void removeSubtypesIf(Predicate<CardSubtype> filter) {
        // Opaque by design: the removal is "what the effect attempts", independent of which
        // subtypes are concretely present.
        log("removeSubtypesIf");
        write(Characteristic.TYPES);
        super.removeSubtypesIf(filter);
    }

    @Override
    public Set<CardColor> getColors() {
        read(Characteristic.COLORS);
        return super.getColors();
    }

    @Override
    public boolean isColorsOverridden() {
        read(Characteristic.COLORS);
        return super.isColorsOverridden();
    }

    @Override
    public Set<CardColor> getSeededColors() {
        read(Characteristic.COLORS);
        return super.getSeededColors();
    }

    @Override
    public void addColor(CardColor color) {
        log("addColor:" + color);
        write(Characteristic.COLORS);
        super.addColor(color);
    }

    @Override
    public void replaceSeedColors(Collection<CardColor> replacement) {
        write(Characteristic.COLORS);
        super.replaceSeedColors(replacement);
    }

    @Override
    public void overrideColors(Collection<CardColor> replacement) {
        log("overrideColors:" + sorted(replacement));
        write(Characteristic.COLORS);
        super.overrideColors(replacement);
    }

    @Override
    public void snapshotSeededCharacteristics() {
        write(Characteristic.COLORS);
        write(Characteristic.ABILITIES);
        super.snapshotSeededCharacteristics();
    }

    @Override
    public Set<Keyword> getKeywords() {
        read(Characteristic.ABILITIES);
        return super.getKeywords();
    }

    @Override
    public boolean hasKeyword(Keyword keyword) {
        read(Characteristic.ABILITIES);
        return super.hasKeyword(keyword);
    }

    @Override
    public int getFlankingInstances() {
        read(Characteristic.ABILITIES);
        return super.getFlankingInstances();
    }

    @Override
    public int getMeleeInstances() {
        read(Characteristic.ABILITIES);
        return super.getMeleeInstances();
    }

    @Override
    public Map<Keyword, Integer> getTriggerKeywordInstances() {
        read(Characteristic.ABILITIES);
        return super.getTriggerKeywordInstances();
    }

    @Override
    public int triggerKeywordInstances(Keyword keyword) {
        read(Characteristic.ABILITIES);
        return super.triggerKeywordInstances(keyword);
    }

    @Override
    public boolean isSeeded() {
        read(Characteristic.ABILITIES);
        return super.isSeeded();
    }

    @Override
    public Set<Keyword> getBlockedKeywords() {
        read(Characteristic.ABILITIES);
        return super.getBlockedKeywords();
    }

    @Override
    public Set<Keyword> getSeededKeywords() {
        read(Characteristic.ABILITIES);
        return super.getSeededKeywords();
    }

    @Override
    public Set<CardColor> getProtectionColors() {
        read(Characteristic.ABILITIES);
        return super.getProtectionColors();
    }

    @Override
    public Set<CardColor> getRemovedProtectionColors() {
        read(Characteristic.ABILITIES);
        return super.getRemovedProtectionColors();
    }

    @Override
    public boolean isProtectionRemoved() {
        read(Characteristic.ABILITIES);
        return super.isProtectionRemoved();
    }

    @Override
    public boolean hasProtectionColor(CardColor color) {
        read(Characteristic.ABILITIES);
        return super.hasProtectionColor(color);
    }

    @Override
    public List<ActivatedAbility> getGrantedActivatedAbilities() {
        read(Characteristic.ABILITIES);
        return super.getGrantedActivatedAbilities();
    }

    @Override
    public List<CardEffect> getGrantedStaticEffects() {
        read(Characteristic.ABILITIES);
        return super.getGrantedStaticEffects();
    }

    @Override
    public Set<CardEffect> getNonAbilityEffects() {
        read(Characteristic.ABILITIES);
        return super.getNonAbilityEffects();
    }

    @Override
    public boolean isLosesAllAbilities() {
        read(Characteristic.ABILITIES);
        return super.isLosesAllAbilities();
    }

    @Override
    public long getLosesAllAbilitiesTimestamp() {
        read(Characteristic.ABILITIES);
        return super.getLosesAllAbilitiesTimestamp();
    }

    @Override
    public boolean isLosesAllNonManaAbilities() {
        read(Characteristic.ABILITIES);
        return super.isLosesAllNonManaAbilities();
    }

    @Override
    public long getLosesAllNonManaAbilitiesTimestamp() {
        read(Characteristic.ABILITIES);
        return super.getLosesAllNonManaAbilitiesTimestamp();
    }

    @Override
    public boolean isAbilityGainProhibited() {
        read(Characteristic.ABILITIES);
        return super.isAbilityGainProhibited();
    }

    @Override
    public boolean isPrintedAbilitiesRemoved() {
        read(Characteristic.ABILITIES);
        return super.isPrintedAbilitiesRemoved();
    }

    @Override
    public void snapshotPrintedTriggerKeywords() {
        write(Characteristic.ABILITIES);
        super.snapshotPrintedTriggerKeywords();
    }

    @Override
    public void addPerpetualTriggerKeywordInstances(Keyword keyword, int extra) {
        write(Characteristic.ABILITIES);
        super.addPerpetualTriggerKeywordInstances(keyword, extra);
    }

    @Override
    public void addKeyword(Keyword keyword) {
        log("addKeyword:" + keyword);
        write(Characteristic.ABILITIES);
        super.addKeyword(keyword);
    }

    @Override
    public void addKeywords(Collection<Keyword> granted) {
        log("addKeywords:" + sorted(granted));
        write(Characteristic.ABILITIES);
        super.addKeywords(granted);
    }

    @Override
    public void removeKeyword(Keyword keyword) {
        log("removeKeyword:" + keyword);
        write(Characteristic.ABILITIES);
        super.removeKeyword(keyword);
    }

    @Override
    public void blockKeyword(Keyword keyword) {
        log("blockKeyword:" + keyword);
        write(Characteristic.ABILITIES);
        super.blockKeyword(keyword);
    }

    @Override
    public void addProtectionColors(Collection<CardColor> colors) {
        log("addProtectionColors:" + sorted(colors));
        write(Characteristic.ABILITIES);
        super.addProtectionColors(colors);
    }

    @Override
    public void removeProtectionColors(Collection<CardColor> colors) {
        write(Characteristic.ABILITIES);
        super.removeProtectionColors(colors);
    }

    @Override
    public void removeAllProtection() {
        write(Characteristic.ABILITIES);
        super.removeAllProtection();
    }

    @Override
    public void addActivatedAbility(ActivatedAbility ability) {
        log("addActivatedAbility:" + ability.getEffects() + ":" + ability.getManaCost()
                + ":" + ability.isRequiresTap() + ":" + ability.getTargetFilter()
                + ":" + ability.getMultiTargetFilters() + ":" + ability.getMinTargets()
                + ":" + ability.getMaxTargets() + ":" + ability.getTimingRestriction()
                + ":" + ability.getGrantSourcePermanentId() + ":" + ability.getDescription());
        write(Characteristic.ABILITIES);
        super.addActivatedAbility(ability);
    }

    @Override
    public void removeActivatedAbilitiesGrantedBy(UUID sourcePermanentId, String description) {
        write(Characteristic.ABILITIES);
        super.removeActivatedAbilitiesGrantedBy(sourcePermanentId, description);
    }

    @Override
    public void addStaticEffect(CardEffect effect) {
        log("addStaticEffect:" + effect);
        write(Characteristic.ABILITIES);
        super.addStaticEffect(effect);
    }

    @Override
    public void addNonAbilityEffect(CardEffect effect) {
        write(Characteristic.ABILITIES);
        super.addNonAbilityEffect(effect);
    }

    @Override
    public void loseAllAbilities(long timestamp) {
        // No timestamp in the token: the effect always attempts the same removal.
        log("loseAllAbilities");
        write(Characteristic.ABILITIES);
        super.loseAllAbilities(timestamp);
    }

    @Override
    public void preventAbilityGain() {
        write(Characteristic.ABILITIES);
        super.preventAbilityGain();
    }

    @Override
    public void loseAllNonManaAbilities(long timestamp) {
        write(Characteristic.ABILITIES);
        super.loseAllNonManaAbilities(timestamp);
    }

    @Override
    public void removePrintedAbilities() {
        log("removePrintedAbilities");
        write(Characteristic.ABILITIES);
        super.removePrintedAbilities();
    }

    @Override
    public int getBasePower() {
        read(Characteristic.POWER_TOUGHNESS);
        return super.getBasePower();
    }

    @Override
    public int getBaseToughness() {
        read(Characteristic.POWER_TOUGHNESS);
        return super.getBaseToughness();
    }

    @Override
    public int getPowerDelta() {
        read(Characteristic.POWER_TOUGHNESS);
        return super.getPowerDelta();
    }

    @Override
    public int getToughnessDelta() {
        read(Characteristic.POWER_TOUGHNESS);
        return super.getToughnessDelta();
    }

    @Override
    public int getSwitchCount() {
        read(Characteristic.POWER_TOUGHNESS);
        return super.getSwitchCount();
    }

    @Override
    public int getEffectivePower() {
        read(Characteristic.POWER_TOUGHNESS);
        return super.getEffectivePower();
    }

    @Override
    public int getEffectiveToughness() {
        read(Characteristic.POWER_TOUGHNESS);
        return super.getEffectiveToughness();
    }

    @Override
    public boolean isSwitched() {
        read(Characteristic.POWER_TOUGHNESS);
        return super.isSwitched();
    }

    @Override
    public void setBasePower(int basePower) {
        write(Characteristic.POWER_TOUGHNESS);
        super.setBasePower(basePower);
    }

    @Override
    public void setBaseToughness(int baseToughness) {
        write(Characteristic.POWER_TOUGHNESS);
        super.setBaseToughness(baseToughness);
    }

    @Override
    public void setPowerDelta(int powerDelta) {
        write(Characteristic.POWER_TOUGHNESS);
        super.setPowerDelta(powerDelta);
    }

    @Override
    public void setToughnessDelta(int toughnessDelta) {
        write(Characteristic.POWER_TOUGHNESS);
        super.setToughnessDelta(toughnessDelta);
    }

    @Override
    public void addPowerToughnessDelta(int power, int toughness) {
        log("addPowerToughnessDelta:" + power + "/" + toughness);
        write(Characteristic.POWER_TOUGHNESS);
        super.addPowerToughnessDelta(power, toughness);
    }

    @Override
    public void switchPowerToughness() {
        log("switchPowerToughness");
        write(Characteristic.POWER_TOUGHNESS);
        super.switchPowerToughness();
    }

    @Override
    public boolean isTokenized() {
        read(Characteristic.TOKEN);
        return super.isTokenized();
    }

    @Override
    public void setTokenized(boolean tokenized) {
        write(Characteristic.TOKEN);
        super.setTokenized(tokenized);
    }
}
