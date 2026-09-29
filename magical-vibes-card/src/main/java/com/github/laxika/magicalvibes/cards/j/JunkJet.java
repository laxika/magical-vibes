package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.EnchantedPermanentPower;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "60")
@CardRegistration(set = "PIP", collectorNumber = "387")
@CardRegistration(set = "PIP", collectorNumber = "588")
@CardRegistration(set = "PIP", collectorNumber = "915")
public class JunkJet extends Card {

    public JunkJet() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, junkToken());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsArtifactPredicate(), "Sacrifice another artifact"),
                        new BoostReferencedPermanentEffect(
                                PermanentReference.ATTACHED, new EnchantedPermanentPower(), new Fixed(0))
                ),
                "{3}, Sacrifice another artifact: Double equipped creature's power until end of turn."
        ));

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }

    private static CreateTokenEffect junkToken() {
        return CreateTokenEffect.ofArtifactToken(
                1,
                "Junk",
                List.of(CardSubtype.JUNK),
                List.of(new ActivatedAbility(
                        true,
                        null,
                        List.of(new SacrificeSelfCost(), new ExileTopCardMayPlayThisTurnEffect(false)),
                        "{T}, Sacrifice this token: Exile the top card of your library. You may play that card this turn. "
                                + "Activate only as a sorcery.",
                        ActivationTimingRestriction.SORCERY_SPEED)));
    }
}
