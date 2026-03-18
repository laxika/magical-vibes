package com.github.laxika.magicalvibes.model.effect;

/**
 * Gains control of target permanent for as long as you control the source permanent
 * (e.g. Olivia Voldaren: "Gain control of target Vampire for as long as you control
 * Olivia Voldaren."). The control effect ends when the source permanent leaves the
 * battlefield or changes controllers.
 *
 * <p>Tracked via {@code GameData.sourceDependentStolenCreatures} (stolen creature ID
 * → source permanent ID). The {@code AuraAttachmentService.returnStolenCreatures()}
 * check verifies the source is still on the same controller's battlefield.
 */
public record GainControlOfTargetPermanentWhileSourceEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
