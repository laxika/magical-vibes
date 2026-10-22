package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsKindredPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "39")
public class WorldQueller extends Card {

    public WorldQueller() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new ChooseOneEffect(List.of(
                        sacrificeMode("Artifact", new PermanentIsArtifactPredicate()),
                        sacrificeMode("Battle", new PermanentIsBattlePredicate()),
                        sacrificeMode("Conspiracy", noPermanentType()),
                        sacrificeMode("Creature", creatureType()),
                        sacrificeMode("Dungeon", noPermanentType()),
                        sacrificeMode("Enchantment", new PermanentIsEnchantmentPredicate()),
                        sacrificeMode("Instant", noPermanentType()),
                        sacrificeMode("Kindred", new PermanentIsKindredPredicate()),
                        sacrificeMode("Land", new PermanentIsLandPredicate()),
                        sacrificeMode("Phenomenon", noPermanentType()),
                        sacrificeMode("Plane", noPermanentType()),
                        sacrificeMode("Planeswalker", new PermanentIsPlaneswalkerPredicate()),
                        sacrificeMode("Scheme", noPermanentType()),
                        sacrificeMode("Sorcery", noPermanentType()),
                        sacrificeMode("Vanguard", noPermanentType())
                )),
                "Choose a card type?"
        ));
    }

    private static ChooseOneEffect.ChooseOneOption sacrificeMode(String label, PermanentPredicate filter) {
        return new ChooseOneEffect.ChooseOneOption(label,
                new SacrificePermanentsEffect(1, filter, SacrificeRecipient.EACH_PLAYER));
    }

    private static PermanentPredicate noPermanentType() {
        return new PermanentNotPredicate(new PermanentTruePredicate());
    }

    private static PermanentPredicate creatureType() {
        return new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate()));
    }
}
