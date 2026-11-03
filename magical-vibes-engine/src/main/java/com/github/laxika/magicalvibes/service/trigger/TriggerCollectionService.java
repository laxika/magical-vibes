package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.BendingType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedSpellCastTrigger;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.TargetedCreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.CreatureEntersTriggerWatcher;
import com.github.laxika.magicalvibes.model.DamagedCreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.Boon;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.DelayedEffectOnDeath;
import com.github.laxika.magicalvibes.model.DelayedReturnOnDeath;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemArtifactGraveyardReturnTriggerEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;
import com.github.laxika.magicalvibes.model.rules.PermanentChange;
import com.github.laxika.magicalvibes.model.rules.PermanentChangeEvent;
import com.github.laxika.magicalvibes.model.rules.RulesObserver;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LifeGainOpponentLifeLossWatcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCopyRider;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingSourceDamage;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentPowerGreaterThanSourcePower;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.action.DelayedExileReturnCounterTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeSourceWhenTargetLeaves;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealsDamage;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealtDamage;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealtDamageByAttackingCreature;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.SacrificeBoonWatcher;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.VisualCueType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureBecomesCopyOfExiledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorForSourceEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AttachedCreatureBecomesCopyOfExiledCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChooseCardNameAtResolutionEffectHandler;
import com.github.laxika.magicalvibes.model.effect.CommanderCreaturesDealPowerDamageToPreventedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetControllerIsTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.OptionalTargetEffect;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AttackingCreaturesAwareEffect;
import com.github.laxika.magicalvibes.model.effect.AttackingPermanentSnapshot;
import com.github.laxika.magicalvibes.model.effect.PerDamageSourceTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.LeavingPermanentIdAwareEffect;
import com.github.laxika.magicalvibes.model.effect.LeavingPermanentCountersAwareEffect;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfSourceDealtDamageToPlayerThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeredModalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerAwareEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDamagedCreatureControllerAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DamagedCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureDamagedByMatchingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.EquipmentDamagesOtherDefendingCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EquipmentTapsAndLocksDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ReflectAllyDamageToDamagedCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.effect.TapAndSkipUntapDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringSpellControllerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellReferencingEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextSpellCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfConditionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfCasualtyPaidEffect;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellForXValueEffect;
import com.github.laxika.magicalvibes.model.effect.DemonstrateEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.effect.NextMatchingSpellCostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCastTriggerToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerGrant;
import com.github.laxika.magicalvibes.model.effect.PutVoyageCounterOnExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnVoyagingCardFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.effect.CombatDamageTriggerContextEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemLifeGainTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardOnAllyLandEntersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnPerCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.ReselectTargetAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.TargetChoiceTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureCardAwareEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureCounterAwareEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDyingCreatureToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.LeavingCreatureNameAwareEffect;
import com.github.laxika.magicalvibes.model.effect.LeavingCreatureCountersAwareEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureCountersAwareEffect;
import com.github.laxika.magicalvibes.model.effect.StormCopyEffect;
import com.github.laxika.magicalvibes.model.effect.OtherInstantsAndSorceriesCastCountEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfWhenChosenPermanentLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringNonTapAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringNinjutsuAbilityConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.ClashOutcomeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellingEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessSacrificesEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureExactStatsConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamagedPlayerOnlyEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.condition.ExactlyAttackers;
import com.github.laxika.magicalvibes.model.condition.ImprintedCardNameMatchesEnteringPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.SourceHasChosenMode;
import com.github.laxika.magicalvibes.model.condition.TreasureManaSpentToActivate;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.BatchedCombatDamageToYouTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfThisPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfDamagedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellWithSuspendCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentFirstSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DamagedPlayerControlsTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToManaSpentToCastToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnAllyCreatureEntersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnAllyLandEntersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureAttachmentsAwareEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemArtifactEntersTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemArtifactTapTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EvolveTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.IncrementTriggerEffect;
import com.github.laxika.magicalvibes.service.event.VisualCueService;
import com.github.laxika.magicalvibes.service.effect.TextChangeTransformer;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggerCandidate;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TargetRedirectionSupport;
import com.github.laxika.magicalvibes.model.effect.MayFightTargetCreatureOnAllyCreatureEntersEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEqualToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.TemporaryGlobalTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellOpponentControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnimationSupport;
import com.github.laxika.magicalvibes.service.effect.turnup.TurnFaceUpCopyService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.effect.OncePerTurnTriggerSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Thin orchestrator that detects trigger events, iterates permanents/effect-slots,
 * and delegates per-effect handling to the {@link TriggerCollectorRegistry}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerCollectionService {

    private final TriggerCollectorRegistry registry;
    private final GameOutcomeService gameOutcomeService;
    private final PlayerInputService playerInputService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TargetLegalityService targetLegalityService;
    private final ValidTargetService validTargetService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final ETBTokenTargetService etbTokenTargetService;
    private final GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    @Autowired
    @Lazy
    private TargetRedirectionSupport targetRedirectionSupport;
    @Autowired
    private ObjectProvider<StateTriggerService> stateTriggerService;
    @Autowired
    private ObjectProvider<VisualCueService> visualCueService;
    @Autowired
    @Lazy
    private AttachedCreatureBecomesCopyOfExiledCreatureEffectHandler attachedCreatureCopyHandler;
    @Autowired
    @Lazy
    private ChooseCardNameAtResolutionEffectHandler chooseCardNameHandler;
    @Autowired
    @Lazy
    private DeathTriggerCollectorService deathTriggerCollectorService;
    @Autowired
    @Lazy
    private TurnFaceUpCopyService turnFaceUpCopyService;
    @Autowired
    @Lazy
    private AnimationSupport animationSupport;

    public List<CardEffect> grantedTriggeredEffects(GameData gameData, Permanent permanent, EffectSlot slot) {
        return grantedTriggeredAbilitySupport.grantedTriggeredEffects(gameData, permanent, slot);
    }

    /** {@link GrantedTriggeredAbilitySupport#grantedTriggerEffects}. */
    public List<CardEffect> grantedTriggerEffects(GameData gameData, Permanent permanent, Trigger trigger) {
        return grantedTriggeredAbilitySupport.grantedTriggerEffects(gameData, permanent, trigger);
    }

    /** {@link GrantedTriggeredAbilitySupport#grantedZoneChangeAbilities}. */
    public List<TriggeredAbility> grantedZoneChangeAbilities(GameData gameData, Permanent permanent) {
        return grantedTriggeredAbilitySupport.grantedZoneChangeAbilities(gameData, permanent);
    }

    public void checkYouPutCountersTriggers(GameData gameData, UUID placingPlayerId, int amount) {
        if (placingPlayerId == null || amount <= 0) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.CountersPlaced(placingPlayerId, amount);
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            dispatchWatch(gameData, permanent, placingPlayerId,
                    CounterWatch.ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER, context);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)
                && !gameData.interaction.isAwaitingInput()) {
            processNextSpellTargetTrigger(gameData);
        }
    }

    /** Fires triggers for one time-counter placement event on a permanent the player controls. */
    public void checkYouPutTimeCountersTriggers(GameData gameData, Permanent target,
                                                UUID placingPlayerId, int amount) {
        if (target == null || placingPlayerId == null || amount <= 0
                || !placingPlayerId.equals(gameQueryService.findPermanentController(gameData, target.getId()))) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.TimeCountersPlaced(target, placingPlayerId, amount);
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            dispatchWatch(gameData, permanent, placingPlayerId,
                    CounterWatch.ON_YOU_PUT_TIME_COUNTERS_ON_CONTROLLED_PERMANENT, context);
        }
    }

    /** Fires triggers for each lore counter placed on a Saga the player controls. */
    public void checkYouPutLoreCounterOnSagaTriggers(GameData gameData, Permanent saga,
                                                     UUID placingPlayerId) {
        if (saga == null || placingPlayerId == null || !saga.getCard().isSaga()
                || !placingPlayerId.equals(gameQueryService.findPermanentController(gameData, saga.getId()))) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(placingPlayerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.LoreCounterPlaced(saga, placingPlayerId);
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            dispatchWatch(gameData, permanent, placingPlayerId,
                    CounterWatch.ON_YOU_PUT_LORE_COUNTERS_ON_SAGA, context);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)
                && !gameData.interaction.isAwaitingInput()) {
            processNextSpellTargetTrigger(gameData);
        }
    }

    /** Fires +1/+1 counter triggers on cards in the controlled creature's controller's graveyard. */
    public void checkGraveyardAllyPlusOnePlusOneCountersPutOnCreatureTriggers(
            GameData gameData, Permanent creature, UUID creatureControllerId, int amount) {
        if (creature == null || creatureControllerId == null || amount <= 0
                || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(creatureControllerId);
        if (graveyard == null) {
            return;
        }

        for (Card card : new ArrayList<>(graveyard)) {
            List<CardEffect> effects = gameQueryService.getEffectiveGraveyardTriggerEffects(
                    gameData, card,
                    CounterWatch.GRAVEYARD_ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_CREATURE.trigger());
            if (effects == null || effects.isEmpty()) {
                continue;
            }

            for (CardEffect effect : effects) {
                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(card, creatureControllerId, may, card.getId());
                } else {
                    StackEntry trigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            creatureControllerId,
                            card.getName() + "'s ability",
                            new ArrayList<>(List.of(effect)));
                    trigger.setTriggeringCardId(card.getId());
                    trigger.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                    gameData.enqueueTrigger(trigger);
                }
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} graveyard trigger queued on +1/+1 counter placement",
                        gameData.id, card.getName());
            }
        }
    }

    public void checkLoyaltyCounterRemovalTriggers(GameData gameData) {
        gameData.forEachPermanent((controllerId, permanent) -> {
            int removedCounters = permanent.drainCountersRemovedSinceTriggerCheck();
            if (removedCounters > 0) {
                TriggerContext context = new TriggerContext.CountersRemovedFromPermanent(
                        permanent, removedCounters);
                for (Permanent watcher : new ArrayList<>(
                        gameData.playerBattlefields.getOrDefault(controllerId, List.of()))) {
                    dispatchWatch(gameData, watcher, controllerId,
                            CounterWatch.ON_ALLY_COUNTERS_REMOVED_FROM_PERMANENT, context);
                }
            }

            int timeCountersRemoved = permanent.drainTimeCountersRemovedSinceTriggerCheck();
            if (timeCountersRemoved > 0) {
                dispatchWatch(gameData, permanent, controllerId,
                        CounterWatch.ON_SELF_TIME_COUNTERS_REMOVED,
                        new TriggerContext.TimeCountersRemoved(permanent, timeCountersRemoved));
            }

            int plusOneRemoved = permanent.drainPlusOnePlusOneCountersRemovedSinceTriggerCheck();
            if (plusOneRemoved > 0) {
                dispatchWatch(gameData, permanent, controllerId,
                        CounterWatch.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTER_REMOVED,
                        new TriggerContext.PlusOnePlusOneCountersRemoved(permanent, plusOneRemoved));
            }

            int amount = permanent.drainLoyaltyCountersRemovedSinceTriggerCheck();
            if (amount <= 0) return;

            dispatchWatch(gameData, permanent, controllerId,
                    CounterWatch.ON_SELF_LOYALTY_COUNTERS_REMOVED,
                    new TriggerContext.LoyaltyCountersRemoved(permanent, amount));
        });
    }

    /** Fires control-change triggers before the changed permanent moves to its new battlefield. */
    public void checkOpponentGainsControlTriggers(GameData gameData, Permanent changedPermanent,
                                                   UUID previousControllerId, UUID newControllerId) {
        if (changedPermanent == null || previousControllerId == null || newControllerId == null
                || previousControllerId.equals(newControllerId)
                || !newControllerId.equals(gameQueryService.getOpponentId(gameData, previousControllerId))) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(previousControllerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.PermanentControlChanged(
                changedPermanent, previousControllerId, newControllerId);
        for (Permanent permanent : new ArrayList<>(battlefield)) {
            dispatchWatch(gameData, permanent, previousControllerId,
                    MiscWatch.ON_OPPONENT_GAINS_CONTROL_OF_YOUR_PERMANENT, context);
        }
    }

    public void checkTimeCounterRemovedFromExiledCardTriggers(GameData gameData, Card card,
            UUID ownerId, int remainingCounters) {
        checkTimeCounterRemovedFromExiledCardTriggers(gameData, card, ownerId, remainingCounters, null);
    }

    /** As above, naming the player who removed the counter ({@code null} for the card's owner). */
    public void checkTimeCounterRemovedFromExiledCardTriggers(GameData gameData, Card card,
            UUID ownerId, int remainingCounters, UUID removerId) {
        if (card == null || ownerId == null) {
            return;
        }

        TriggerContext context = new TriggerContext.TimeCounterRemovedFromExile(remainingCounters, removerId);
        for (CardEffect effect : CounterWatch.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE.effectsOf(card)) {
            dispatch(new TriggerMatchContext(gameData, null, ownerId, effect, card),
                    CounterWatch.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE, effect, context);
        }
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId, true);
    }

    public void checkControllerGivesGiftTriggers(GameData gameData, UUID giverId) {
        TriggerContext context = new TriggerContext.GiftGiven(giverId);
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (controllerId.equals(giverId)) {
                dispatchWatch(gameData, permanent, controllerId, ActionWatch.ON_CONTROLLER_GIVES_GIFT, context);
            }
        });
    }

    public void checkSpellCopyTriggers(GameData gameData, StackEntry copiedSpell) {
        if (copiedSpell.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                || copiedSpell.getEntryType() == StackEntryType.ACTIVATED_ABILITY) {
            return;
        }

        UUID copyingPlayerId = copiedSpell.getControllerId();
        TriggerContext.SpellCopy context = new TriggerContext.SpellCopy(copiedSpell, copyingPlayerId);
        gameData.forEachPermanent((playerId, permanent) -> {
            if (playerId.equals(copyingPlayerId)) {
                dispatchWatch(gameData, permanent, playerId, SpellAbilityWatch.ON_CONTROLLER_COPIES_SPELL, context);
            } else {
                dispatchWatch(gameData, permanent, playerId, SpellAbilityWatch.ON_OPPONENT_COPIES_SPELL, context);
            }
        });
    }

    /**
     * Fires delayed "until end of turn, whenever you cast a [filter] spell, …" triggers registered
     * this turn (Mountain Titan). One trigger per registration. Registrations that require their
     * source permanent to remain on the battlefield are skipped once it has left.
     */
    private void processDelayedControllerSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        if (!gameData.hasDelayedAction(DelayedControllerSpellCastTrigger.class)) {
            return;
        }
        for (DelayedControllerSpellCastTrigger delayed
                : gameData.getDelayedActions(DelayedControllerSpellCastTrigger.class)) {
            if (!delayed.controllerId().equals(castingPlayerId)) continue;
            Permanent source = delayed.sourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, delayed.sourcePermanentId());
            if (delayed.sourceMustRemainOnBattlefield() && source == null) {
                continue;
            }
            StackEntry spellEntry = gameData.stack.stream()
                    .filter(candidate -> spellCard.getId().equals(candidate.getCard().getId()))
                    .findFirst()
                    .orElse(null);
            // A spell with {X} on the stack is evaluated with its chosen X (CR 202.3e).
            Integer spellXValue = spellEntry != null && spellCard.getParsedManaCost() != null
                    && spellCard.getParsedManaCost().hasX() ? spellEntry.getXValue() : null;
            if (delayed.spellFilter() != null
                    && !predicateEvaluationService.matchesCardPredicate(
                            spellCard, delayed.spellFilter(), null, gameData, castingPlayerId,
                            null, null, spellXValue)) {
                continue;
            }
            if (delayed.stackEntryFilter() != null
                    && (spellEntry == null
                    || !targetLegalityService.matchesStackEntryPredicate(
                    gameData, spellEntry, delayed.stackEntryFilter(), castingPlayerId,
                    source != null ? source : delayed.sourcePermanentSnapshot(), null,
                    source == null ? delayed.sourcePowerAtLastKnown() : null))) {
                continue;
            }

            if (delayed.oneShot()) {
                gameData.delayedActions.remove(delayed);
            }

            if (delayed.targetFilter() != null) {
                boolean optionalTarget = delayed.resolvedEffects().stream()
                        .anyMatch(effect -> effect instanceof OptionalTargetEffect || effect.hasOptionalTarget());
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        new ArrayList<>(delayed.resolvedEffects()),
                        delayed.targetFilter() instanceof PlayerPredicateTargetFilter,
                        delayed.targetFilter(),
                        0,
                        delayed.sourcePermanentId(),
                        delayed.sourcePermanentSnapshot(),
                        optionalTarget,
                        null,
                        null,
                        delayed.controllerId(),
                        spellManaValue(gameData, spellCard),
                        null,
                        false));
                gameLogService.append(gameData, GameLog.cardTextCard(
                        delayed.sourceCard(), "'s delayed trigger fires for ", spellCard, "."));
                log.info("Game {} - {} delayed spell-cast trigger awaits a target for {}",
                        gameData.id, delayed.sourceCard().getName(), spellCard.getName());
                continue;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    delayed.resolvedEffects().stream().map(effect -> effect instanceof CopyTriggeringSpellEffect copy
                            ? (CardEffect) new CopyTriggeringSpellEffect(copy.tokenCopy(), copy.retargetToSource(),
                            spellEntry == null ? null : new StackEntry(spellEntry), copy.mayChooseNewTargets())
                            : effect instanceof TriggeringSpellManaValueEffect manaValueAware
                            ? manaValueAware.snapshotTriggeringSpellManaValue(spellManaValue(gameData, spellCard))
                            : effect).toList(),
                    null,
                    delayed.sourcePermanentId());
            entry.setTriggeringCardId(spellCard.getId());
            if (spellEntry != null) {
                entry.setXValue(spellEntry.getXValue());
            }
            entry.setEventValue(spellManaValue(gameData, spellCard));
            entry.setSourcePermanentSnapshot(delayed.sourcePermanentSnapshot());
            entry.setNonTargeting(true);
            gameData.stack.add(entry);
            gameLogService.append(gameData, GameLog.cardTextCard(
                    delayed.sourceCard(), "'s delayed trigger fires for ", spellCard, "."));
            log.info("Game {} - {} delayed spell-cast trigger fires for {}",
                    gameData.id, delayed.sourceCard().getName(), spellCard.getName());
        }
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId, boolean castFromHand) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId,
                castFromHand ? Zone.HAND : Zone.GRAVEYARD);
    }

    /**
     * Fires triggers caused by a scheme being set in motion. The scheme ability must already be
     * on the stack so effects that repeat it can snapshot the exact set-in-motion ability.
     */
    public void checkSchemeSetInMotionTriggers(GameData gameData, StackEntry schemeEntry) {
        if (schemeEntry == null || schemeEntry.getCard() == null
                || schemeEntry.getControllerId() == null
                || !schemeEntry.getCard().hasType(CardType.SCHEME)) {
            return;
        }

        UUID settingPlayerId = schemeEntry.getControllerId();
        int schemesSet = 1;
        Integer pendingReplacements = gameData.pendingNextSchemeSetInMotionReplacements.get(settingPlayerId);
        if (pendingReplacements != null) {
            if (pendingReplacements <= 1) {
                gameData.pendingNextSchemeSetInMotionReplacements.remove(settingPlayerId);
            } else {
                gameData.pendingNextSchemeSetInMotionReplacements.put(settingPlayerId,
                        pendingReplacements - 1);
            }
            schemesSet = 3;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(settingPlayerId, List.of());

        for (int schemeIndex = 0; schemeIndex < schemesSet; schemeIndex++) {
            TriggerContext context = new TriggerContext.SchemeSetInMotion(schemeEntry, settingPlayerId);
            for (Permanent permanent : List.copyOf(battlefield)) {
                dispatchWatch(gameData, permanent, settingPlayerId,
                        ActionWatch.ON_CONTROLLER_SETS_SCHEME_IN_MOTION, context);
            }
            dispatchFaceUpSchemes(gameData, ActionWatch.ON_CONTROLLER_SETS_SCHEME_IN_MOTION, context,
                    settingPlayerId::equals);
        }
    }

    public void checkCrimeTriggers(GameData gameData, StackEntry stackEntry) {
        if (!gameData.isCommittedCrime(stackEntry)) return;

        UUID committingPlayerId = stackEntry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(committingPlayerId);
        TriggerContext ctx = new TriggerContext.Crime(committingPlayerId);
        if (battlefield != null) {
            for (Permanent permanent : List.copyOf(battlefield)) {
                dispatchWatch(gameData, permanent, committingPlayerId,
                        ActionWatch.ON_CONTROLLER_COMMITS_CRIME, ctx);
            }
        }

        gameData.forEachBattlefield((controllerId, opponentsBattlefield) -> {
            if (controllerId.equals(committingPlayerId)) return;
            for (Permanent permanent : List.copyOf(opponentsBattlefield)) {
                dispatchWatch(gameData, permanent, controllerId,
                        ActionWatch.ON_OPPONENT_COMMITS_CRIME, ctx);
            }
        });

        List<Card> graveyard = gameData.playerGraveyards.get(committingPlayerId);
        if (graveyard != null) {
            for (Card card : List.copyOf(graveyard)) {
                for (CardEffect effect : gameQueryService.getEffectiveGraveyardTriggerEffects(
                        gameData, card, ActionWatch.GRAVEYARD_ON_CONTROLLER_COMMITS_CRIME.trigger())) {
                    queueGraveyardCrimeTrigger(gameData, card, committingPlayerId, effect);
                }
            }
        }

    }

    private void queueGraveyardEerieTriggers(GameData gameData, UUID controllerId, Card card,
                                              TriggerWatch watch, Card triggeringCard) {
        List<CardEffect> effects = watch.effectsOf(card);
        if (effects == null || effects.isEmpty()) return;

        for (CardEffect effect : effects) {
            CardEffect resolved = triggeringCard == null
                    ? effect
                    : unwrapTriggeringCardConditional(effect, triggeringCard, gameData, controllerId);
            if (resolved == null) continue;

            if (resolved instanceof MayEffect may) {
                gameData.queueMayAbility(card, controllerId, may);
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(resolved))
                ));
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
            log.info("Game {} - {} graveyard eerie trigger queued", gameData.id, card.getName());
        }
    }

    private void queueGraveyardCrimeTrigger(GameData gameData, Card card, UUID controllerId,
                                             CardEffect effect) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                controllerId,
                card.getName() + "'s ability",
                new ArrayList<>(List.of(effect)));
        entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(card));
    }

    /**
     * Zone-carrying form: {@code castZone} is the zone the spell was cast from, so triggers that
     * care about a specific origin (cast from a graveyard, cast from the top of a library) can tell
     * them apart instead of sharing one "not from hand" flag.
     */
    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId, Zone castZone) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId, castZone, null);
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId,
                                       Zone castZone, UUID exiledSourcePermanentId) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId, castZone, exiledSourcePermanentId, false);
    }

    /**
     * Emits the cosmetic {@link VisualCueType#SPELL_CAST} cue for a spell that was just put on the
     * stack. Targets are public once chosen, so the cue names the first player, the first permanent
     * and the first spell the spell targets; a face-down spell keeps its name and colors hidden.
     */
    private void emitSpellCastCue(GameData gameData, Card spellCard) {
        VisualCueService cues = visualCueService == null ? null : visualCueService.getIfAvailable();
        if (cues == null) {
            return;
        }
        StackEntry spell = null;
        for (StackEntry entry : gameData.stack) {
            if (entry.getCard() != null && entry.getCard().getId().equals(spellCard.getId()) && !entry.isCopy()) {
                spell = entry;
            }
        }
        if (spell == null) {
            return;
        }
        List<UUID> targetIds = new ArrayList<>();
        if (spell.getTargetId() != null) {
            targetIds.add(spell.getTargetId());
        }
        if (spell.getTargetIds() != null) {
            targetIds.addAll(spell.getTargetIds());
        }
        UUID targetPlayerId = null;
        UUID targetPermanentId = null;
        UUID targetCardId = null;
        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) {
                targetPlayerId = targetPlayerId != null ? targetPlayerId : targetId;
            } else if (gameQueryService.findPermanentById(gameData, targetId) != null) {
                targetPermanentId = targetPermanentId != null ? targetPermanentId : targetId;
            } else if (targetCardId == null) {
                targetCardId = targetedSpellCardId(gameData, spell, targetId);
            }
        }
        cues.emit(gameData, VisualCueType.SPELL_CAST, VisualCueService.Source.of(spell),
                targetPermanentId, targetPlayerId, targetCardId, null, GameEventAudience.allPlayers());
    }

    private static UUID targetedSpellCardId(GameData gameData, StackEntry spell, UUID targetId) {
        for (StackEntry entry : gameData.stack) {
            if (entry != spell && entry.getCard() != null && entry.getTargetableId().equals(targetId)
                    && entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                    && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY) {
                return entry.getCard().getId();
            }
        }
        return null;
    }

    /**
     * Intervening-if (CR 603.4) for a zone-resident spell-cast trigger gated on the mana spent to cast the
     * spell: the trigger only goes on the stack when the spell's mana spent meets the threshold.
     */
    private static boolean spellManaSpentGatesMet(SpellCastTriggerEffect trigger, int spellManaSpent) {
        return trigger.resolvedEffects().stream().noneMatch(effect ->
                effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof SpellManaSpentAtLeast spent
                        && spellManaSpent < spent.minMana());
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId,
                                       Zone castZone, UUID exiledSourcePermanentId,
                                       boolean castFaceDown) {
        emitSpellCastCue(gameData, spellCard);
        gameData.initializeCardIntensity(spellCard);
        for (StackEntry spell : gameData.stack) {
            if (spell.getCard() != null && spell.getCard().getId().equals(spellCard.getId()) && !spell.isCopy()) {
                spell.setAdditionalEnterCounters(gameData.spellAdditionalEnterCounters
                        .getOrDefault(spellCard.getId(), 0));
            }
        }
        gameData.recordSpellCastFromZone(castingPlayerId, castZone, spellCard);
        if (castZone == Zone.EXILE && !spellCard.isToken()) {
            gameData.recordCardPlayedFromExile(castingPlayerId);
        }
        if (castZone == Zone.HAND) {
            gameData.recordSpellCastFromHand(spellCard);
        }
        var ctx = new TriggerContext.SpellCast(spellCard, castingPlayerId, castZone,
                exiledSourcePermanentId);
        collectCreatureSpellBoonTriggers(gameData, spellCard, castingPlayerId, castFaceDown);
        if (!castFaceDown && !spellCard.hasType(CardType.CREATURE)) {
            for (Permanent source : gameData.playerBattlefields.getOrDefault(castingPlayerId, List.of())) {
                int prowessInstances = gameQueryService.triggerKeywordInstances(gameData, source, Keyword.PROWESS);
                if (prowessInstances == 0) continue;
                int triggerCount = prowessInstances * (1 + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                        gameData, castingPlayerId, source));
                for (int triggerIndex = 0; triggerIndex < triggerCount; triggerIndex++) {
                    StackEntry prowess = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(),
                        castingPlayerId, source.getCard().getName() + "'s prowess ability",
                        List.of(new com.github.laxika.magicalvibes.model.effect.BoostSelfEffect(1, 1)),
                        null, source.getId());
                    prowess.setSourcePermanentSnapshot(new Permanent(source));
                    gameData.enqueueTrigger(prowess);
                }
            }
        }
        if (castZone != Zone.HAND) {
            gameData.recordSpellCastFromOutsideHand(castingPlayerId);
            gameData.playersWhoPlayedOrCastFromOutsideHandThisTurn.add(castingPlayerId);
        }

        // "The first spell you cast from anywhere other than your hand each turn has demonstrate."
        // The trigger entry must retain the just-cast spell ID because DemonstrateEffect copies
        // that spell when the optional ability resolves.
        if (castZone != Zone.HAND
                && gameData.getSpellsCastFromOutsideHandThisTurnCount(castingPlayerId) == 1) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.DEMONSTRATE_TO_FIRST_SPELL_FROM_OUTSIDE_HAND);
                    if (grantEffects.isEmpty()) continue;

                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            castingPlayerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(grantEffects),
                            null,
                            perm.getId());
                    entry.setTriggeringCardId(spellCard.getId());
                    entry.setNonTargeting(true);
                    gameData.stack.add(entry);
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                }
            }
        }

        if (castZone == Zone.EXILE) {
            gameData.expireFloatingEffects(fe ->
                    fe.duration() == EffectDuration.UNTIL_SOURCE_CARD_CAST_FROM_EXILE
                            && fe.effect() instanceof GrantActivatedAbilityEffect grant
                            && spellCard.getId().equals(grant.expirationCardId()));
        }

        gameData.expireFloatingEffects(fe ->
                (fe.duration() == EffectDuration.UNTIL_MATCHING_SPELL_CAST
                        || fe.duration() == EffectDuration.UNTIL_TARGET_PLAYER_MATCHING_SPELL_CAST)
                        && fe.effect() instanceof NextMatchingSpellCostEffect next
                        && next.appliesToPlayer(castingPlayerId, fe.controllerId())
                        && next.appliesToFaceDownCast(castFaceDown)
                        && predicateEvaluationService.matchesCardPredicate(
                                spellCard, next.predicate(), null, gameData, castingPlayerId));

        if (spellCard.hasType(CardType.CREATURE)) {
            gameData.expireFloatingEffectsOnCreatureSpellCast();
        }

        // Opening hand reveal delayed triggers (Chancellor cycle)
        if (!gameData.openingHandRevealTriggers.isEmpty()
                && !gameData.playersWhoCastFirstSpellInGame.contains(castingPlayerId)) {
            gameData.playersWhoCastFirstSpellInGame.add(castingPlayerId);
            for (OpeningHandRevealTrigger trigger : gameData.openingHandRevealTriggers) {
                if (!trigger.revealingPlayerId().equals(castingPlayerId)
                        && trigger.effect() instanceof CounterUnlessEffect counterEffect) {
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            trigger.sourceCard(),
                            trigger.revealingPlayerId(),
                            trigger.sourceCard().getName() + "'s ability",
                            new ArrayList<>(List.of(counterEffect)),
                            spellCard.getId(),
                            Zone.STACK
                    );
                    gameData.stack.add(entry);
                }
            }
        }

        // ON_ANY_PLAYER_CASTS_SPELL
        gameData.forEachPermanent((playerId, perm) -> {
            dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, ctx);
        });
        dispatchFaceUpSchemes(gameData, SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, ctx, ownerId -> true);

        collectTemporaryGlobalSpellCastTriggers(gameData, castingPlayerId, castZone,
                exiledSourcePermanentId);

        if (gameData.planechase != null && gameData.planechase.controllerId != null) {
            for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
                for (CardEffect effect : SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL.effectsOf(object.getCard())) {
                    registry.dispatch(new TriggerMatchContext(gameData, null,
                                    gameData.planechase.controllerId, effect,
                                    object.getCard(), object.copy()),
                            SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL, effect, ctx);
                }
            }
        }

        // ON_CONTROLLER_CASTS_SPELL (only controller's own spells)
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(castingPlayerId)) return;
            dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, ctx);
        });
        dispatchDepartedManaSourceSpellCastTriggers(gameData, spellCard, castingPlayerId, ctx);
        dispatchFaceUpSchemes(gameData, SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, ctx, castingPlayerId::equals);

        if (gameData.planechase != null && castingPlayerId.equals(gameData.planechase.controllerId)) {
            for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
                for (CardEffect effect : SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL.effectsOf(object.getCard())) {
                    registry.dispatch(new TriggerMatchContext(gameData, null, castingPlayerId, effect,
                                    object.getCard(), object.copy()),
                            SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, effect, ctx);
                }
            }
        }

        collectTemporaryControllerSpellCastTriggers(gameData, spellCard, castingPlayerId,
                castZone, exiledSourcePermanentId);

        processDelayedControllerSpellCastTriggers(gameData, spellCard, castingPlayerId);

        // ON_OPPONENT_CASTS_SPELL (only opponents' permanents)
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(castingPlayerId)) return;
            dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, ctx);
        });
        dispatchFaceUpSchemes(gameData, SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, ctx,
                ownerId -> !ownerId.equals(castingPlayerId));

        dispatchSuspendedExiledCardSpellCastTriggers(gameData, castingPlayerId, ctx);

        // Increment keyword (CR keyword): "Whenever you cast a spell, if the amount of mana you spent
        // is greater than this creature's power or toughness, put a +1/+1 counter on it." Driven by the
        // Scryfall-loaded keyword (like Undying) rather than a per-card effect. Read before mana-spent
        // is cleared below.
        collectIncrementTriggers(gameData, spellCard, castingPlayerId);

        // Emblem spell cast triggers (e.g. Venser's emblem, Jace Unraveler of Secrets' emblem,
        // Chandra Dressed to Kill's emblem). Mana-spent readers must run before clearSpellCastManaSpent.
        for (Emblem emblem : gameData.emblems) {
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof ExileTargetOnControllerSpellCastEffect) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    gameData.queueInteraction(new PermanentChoiceContext.EmblemTriggerTarget(
                            "Venser's emblem",
                            emblem.controllerId(),
                            List.of(new ExileTargetPermanentEffect()),
                            emblem.sourceCard()
                    ));
                } else if (effect instanceof CounterOpponentFirstSpellEachTurnEffect) {
                    // Opponent's first spell this turn — auto-target it on the stack (no choice).
                    if (emblem.controllerId().equals(castingPlayerId)) continue;
                    if (gameData.getSpellsCastThisTurnCount(castingPlayerId) != 1) continue;
                    Card source = emblem.sourceCard();
                    String desc = (source != null ? source.getName() : "Jace, Unraveler of Secrets")
                            + "'s emblem";
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source != null ? source : spellCard,
                            emblem.controllerId(),
                            desc,
                            new ArrayList<>(List.of(new CounterSpellEffect())),
                            spellCard.getId(),
                            Zone.STACK
                    ));
                    gameLogService.append(gameData,
                            GameLog.text(desc + " triggers — counter that spell."));
                    log.info("Game {} - {} counters opponent's first spell this turn",
                            gameData.id, desc);
                } else if (effect instanceof SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect) {
                    // Garruk, Caller of Beasts' emblem — fires on the controller's own creature spells.
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    if (!spellCard.hasType(CardType.CREATURE)) continue;
                    Card source = emblem.sourceCard();
                    String desc = (source != null ? source.getName() : "Garruk, Caller of Beasts")
                            + "'s emblem";
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source != null ? source : spellCard,
                            emblem.controllerId(),
                            desc,
                            new ArrayList<>(List.of(new MayEffect(
                                    new SearchLibraryEffect(
                                            new CardTypePredicate(CardType.CREATURE),
                                            LibrarySearchDestination.BATTLEFIELD),
                                    "Search your library for a creature card and put it onto the battlefield?")))
                    ));
                    gameLogService.append(gameData, GameLog.text(desc + " triggers."));
                    log.info("Game {} - {} creature-spell search trigger queued", gameData.id, desc);
                } else if (effect instanceof MillEffect mill && mill.recipient() == MillRecipient.TARGET_PLAYER) {
                    // "Whenever you cast a spell, target opponent mills N cards" (Jace, Telepath
                    // Unbound's emblem) — the opponent restriction rides the player target filter.
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            sourceCard,
                            emblem.controllerId(),
                            new ArrayList<>(List.of(mill)),
                            true,
                            new PlayerPredicateTargetFilter(
                                    new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                    "Target must be an opponent")
                    ));
                    gameLogService.append(gameData, GameLog.text(
                            (source != null ? source.getName() : "Emblem")
                                    + "'s emblem triggers — choose target opponent."));
                    log.info("Game {} - emblem mill trigger queued", gameData.id);
                } else if (effect instanceof DealDamageEqualToManaSpentToCastToAnyTargetEffect damageTrigger) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    if (damageTrigger.spellFilter() != null
                            && !predicateEvaluationService.matchesCardPredicate(
                                    spellCard, damageTrigger.spellFilter(), null)) {
                        continue;
                    }
                    int manaSpent = gameData.getSpellCastManaSpent(spellCard.getId());
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    String desc = (source != null ? source.getName() : "Chandra, Dressed to Kill")
                            + "'s emblem";
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            sourceCard,
                            emblem.controllerId(),
                            new ArrayList<>(List.of(new DealDamageToAnyTargetEffect(manaSpent)))
                    ));
                    gameLogService.append(gameData,
                            GameLog.text(desc + " triggers — choose a target for " + manaSpent + " damage."));
                    log.info("Game {} - {} emblem mana-spent damage trigger queued ({} damage)",
                            gameData.id, desc, manaSpent);
                } else if (effect instanceof StormEffect storm) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    if (storm.instantOrSorceryOnly()
                            && !spellCard.hasType(CardType.INSTANT)
                            && !spellCard.hasType(CardType.SORCERY)) {
                        continue;
                    }
                    StackEntry spellEntry = gameData.stack.stream()
                            .filter(entry -> entry.getTargetableId().equals(spellCard.getId()))
                            .findFirst().orElse(null);
                    if (spellEntry == null) continue;
                    int copies = Math.max(0, gameData.getTotalSpellsCastThisTurnCount() - 1);
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            sourceCard,
                            emblem.controllerId(),
                            sourceCard.getName() + "'s emblem",
                            new ArrayList<>(List.of(new StormCopyEffect(
                                    new StackEntry(spellEntry), castingPlayerId, copies,
                                    storm.tokenCopy(), storm.removeLegendary())))));
                } else if (effect instanceof CopyControllerCastSpellOnSpellCastEffect copyTrigger) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    if (copyTrigger.spellFilter() != null
                            && !predicateEvaluationService.matchesCardPredicate(
                            spellCard, copyTrigger.spellFilter(), null)) {
                        continue;
                    }
                    StackEntry spellEntry = gameData.stack.stream()
                            .filter(entry -> entry.getTargetableId().equals(spellCard.getId()))
                            .findFirst().orElse(null);
                    if (spellEntry == null) continue;
                    if (copyTrigger.requiredCastWithAdventure()
                            && (!spellEntry.isCastWithAdventure()
                            || (spellEntry.getEntryType() != StackEntryType.INSTANT_SPELL
                            && spellEntry.getEntryType() != StackEntryType.SORCERY_SPELL))) {
                        continue;
                    }
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    CardEffect copyEffect = new CopyControllerCastSpellEffect(
                            new StackEntry(spellEntry), castingPlayerId,
                            copyTrigger.grantedKeywords(), copyTrigger.additionalTypes(),
                            copyTrigger.tokenCopy(), copyTrigger.mayChooseNewTargets());
                    if (copyTrigger.copies() > 1) {
                        copyEffect = new CopyControllerCastSpellEffect(
                                new StackEntry(spellEntry), castingPlayerId,
                                copyTrigger.grantedKeywords(), copyTrigger.additionalTypes(), Set.of(),
                                copyTrigger.tokenCopy(), copyTrigger.mayChooseNewTargets(), false, false,
                                false, false, false, List.of(), null, null, copyTrigger.copies());
                    }
                    CardEffect resolutionEffect = copyTrigger.manaCost() == null
                            ? copyEffect
                            : new MayPayManaEffect(copyTrigger.manaCost(), copyEffect,
                            "Pay " + copyTrigger.manaCost() + " to copy " + spellCard.getName() + "?");
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            sourceCard,
                            emblem.controllerId(),
                            sourceCard.getName() + "'s emblem",
                            new ArrayList<>(List.of(resolutionEffect))));
                } else if (effect instanceof SpellCastTriggerEffect trigger
                        && trigger.castSpellTargetCondition() instanceof StackEntryTargetsPermanentPredicate
                        && trigger.resolvedEffects().size() == 1
                        && trigger.resolvedEffects().getFirst() instanceof GainControlOfTargetEffect controlEffect
                        && controlEffect.duration() == ControlDuration.PERMANENT) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    StackEntry spellEntry = gameData.stack.stream()
                            .filter(entry -> entry.getTargetableId().equals(spellCard.getId()))
                            .findFirst().orElse(null);
                    if (spellEntry == null) continue;
                    List<UUID> targetIds = new ArrayList<>();
                    if (spellEntry.getTargetId() != null) {
                        targetIds.add(spellEntry.getTargetId());
                    }
                    targetIds.addAll(spellEntry.getDeclaredTargetIds());
                    targetIds = targetIds.stream()
                            .distinct()
                            .filter(targetId -> gameQueryService.findPermanentById(gameData, targetId) != null)
                            .toList();
                    if (targetIds.isEmpty()) continue;
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    String desc = (source != null ? source.getName() : "Emblem") + "'s emblem";
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            sourceCard,
                            emblem.controllerId(),
                            desc,
                            new ArrayList<>(List.of(controlEffect)),
                            null,
                            targetIds
                    );
                    entry.setTriggeringCardId(spellCard.getId());
                    entry.setNonTargeting(true);
                    gameData.stack.add(entry);
                    gameLogService.append(gameData, GameLog.text(desc + " triggers."));
                    log.info("Game {} - {} captured {} permanent spell targets for control",
                            gameData.id, desc, targetIds.size());
                } else if (effect instanceof SpellCastTriggerEffect trigger
                        && trigger.resolvedEffects().stream().anyMatch(resolved ->
                        resolved instanceof GivePoisonCountersEffect poison
                                && poison.recipient() == PoisonRecipient.TARGET_PLAYER)) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    if (trigger.spellFilter() != null && !predicateEvaluationService.matchesCardPredicate(
                            spellCard, trigger.spellFilter(), null, gameData, castingPlayerId)) continue;
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            emblem.sourceCard(), emblem.controllerId(), trigger.resolvedEffects(), true,
                            trigger.targetFilter()));
                } else if (effect instanceof SpellCastTriggerEffect trigger
                        && isNonTargetingEmblemSpellCastTrigger(trigger)) {
                    if (!trigger.triggersOnAnyPlayer()
                            && !emblem.controllerId().equals(castingPlayerId)) continue;
                    if (trigger.spellFilter() != null
                            && !predicateEvaluationService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) {
                        continue;
                    }
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    String desc = (source != null ? source.getName() : "Emblem") + "'s emblem";
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            sourceCard,
                            emblem.controllerId(),
                            desc,
                            new ArrayList<>(trigger.resolvedEffects())
                    );
                    entry.setTriggeringCardId(spellCard.getId());
                    entry.setNonTargeting(true);
                    gameData.stack.add(entry);
                    gameLogService.append(gameData, GameLog.text(desc + " triggers."));
                    log.info("Game {} - {} generic emblem spell-cast trigger queued", gameData.id, desc);
                } else if (effect instanceof DealDamageToAnyTargetOnControllerSpellCastEffect damageTrigger) {
                    if (!emblem.controllerId().equals(castingPlayerId)) continue;
                    if (damageTrigger.spellFilter() != null
                            && !predicateEvaluationService.matchesCardPredicate(
                                    spellCard, damageTrigger.spellFilter(), null)) {
                        continue;
                    }
                    Card source = emblem.sourceCard();
                    Card sourceCard = source != null ? source : spellCard;
                    String desc = (source != null ? source.getName() : "Emblem") + "'s emblem";
                    List<CardEffect> resolvedEffects = new ArrayList<>();
                    resolvedEffects.add(new DealDamageToAnyTargetEffect(damageTrigger.damage()));
                    resolvedEffects.addAll(damageTrigger.additionalEffects());
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            sourceCard,
                            emblem.controllerId(),
                            resolvedEffects
                    ));
                    gameLogService.append(gameData,
                            GameLog.text(desc + " triggers — choose a target for " + damageTrigger.damage() + " damage."));
                    log.info("Game {} - {} damage trigger queued ({})",
                            gameData.id, desc, damageTrigger.damage());
                }
            }
        }

        int pathOfAncestryScries = gameData.getSpellCastPathOfAncestryManaSpent(spellCard.getId());
        gameData.clearSpellCastPathOfAncestryManaSpent(spellCard.getId());
        for (int i = 0; i < pathOfAncestryScries; i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    spellCard,
                    castingPlayerId,
                    "Path of Ancestry's ability",
                    new ArrayList<>(List.of(new ScryEffect(1)))
            );
            entry.setTriggeringCardId(spellCard.getId());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
        }
        if (pathOfAncestryScries > 0) {
            gameLogService.append(gameData, GameLog.text("Path of Ancestry triggers."));
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.EmblemTriggerTarget.class)) {
            triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }

        int spellManaSpent = gameData.getSpellCastManaSpent(spellCard.getId());
        boolean castUsingTreasureMana = gameData.spellCastUsedTreasureMana(spellCard.getId());
        gameData.clearSpellCastManaSpent(spellCard.getId());
        gameData.clearSpellCastManaSources(spellCard.getId());
        gameData.clearSpellCastTreasureMana(spellCard.getId());
        gameData.clearSpellCastCreatureManaSpent(spellCard.getId());

        // GRAVEYARD_ON_CONTROLLER_CASTS_SPELL — graveyard-resident spell-cast triggers
        // (e.g. Lingering Phantom: "Whenever you cast a historic spell, you may pay {B}. If you do, return ~ to hand.")
        List<Card> castingPlayerGraveyard = gameData.playerGraveyards.get(castingPlayerId);
        if (castingPlayerGraveyard != null) {
            for (Card card : new ArrayList<>(castingPlayerGraveyard)) {
                List<CardEffect> graveyardEffects = gameQueryService.getEffectiveGraveyardLegacyTriggerEffects(
                        gameData, card, SpellAbilityWatch.GRAVEYARD_ON_CONTROLLER_CASTS_SPELL.trigger());
                if (graveyardEffects == null || graveyardEffects.isEmpty()) continue;

                for (CardEffect effect : graveyardEffects) {
                    if (effect instanceof SpellCastTriggerEffect trigger) {
                        if (!predicateEvaluationService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) continue;
                        if (trigger.castSpellTargetCondition() != null) {
                            StackEntry spellEntry = gameData.stack.stream()
                                    .filter(entry -> entry.getTargetableId().equals(spellCard.getId()))
                                    .findFirst()
                                    .orElse(null);
                            if (spellEntry == null || !targetLegalityService.matchesStackEntryPredicate(
                                    gameData, spellEntry, trigger.castSpellTargetCondition(), castingPlayerId)) {
                                continue;
                            }
                        }
                        if (trigger.nthSpellNumber() > 0 && !isNthMatchingSpell(gameData, trigger, castingPlayerId)) {
                            continue;
                        }
                        if (!spellManaSpentGatesMet(trigger, spellManaSpent)) {
                            continue;
                        }

                        // Bind "source card in graveyard" to this graveyard incarnation (CR 400.7) so the
                        // ability does nothing if the card has left and returned to the graveyard.
                        List<CardEffect> boundEffects = trigger.resolvedEffects().stream()
                                .map(e -> bindSourceCardInGraveyardVersion(gameData, e, card.getId()))
                                .toList();
                        if (trigger.manaCost() != null) {
                            // "you may pay {X}" pattern — queue MayPayManaEffect on the stack
                            CardEffect resolvedEffect = boundEffects.getFirst();
                            MayPayManaEffect mayPay = new MayPayManaEffect(
                                    trigger.manaCost(),
                                    resolvedEffect,
                                    "Pay " + trigger.manaCost() + " to return " + card.getName()
                                            + " from your graveyard to your hand?"
                            );
                            StackEntry mayPayEntry = new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    card,
                                    castingPlayerId,
                                    card.getName() + "'s ability",
                                    new ArrayList<>(List.of(mayPay)),
                                    (UUID) null,
                                    (UUID) null);
                            mayPayEntry.setTriggeringCardId(card.getId());
                            mayPayEntry.setTriggeringCardGraveyardEntryVersion(
                                    gameData.graveyardEntryVersion(card.getId()));
                            gameData.stack.add(mayPayEntry);
                        } else {
                            StackEntry entry = new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    card,
                                    castingPlayerId,
                                    card.getName() + "'s ability",
                                    new ArrayList<>(boundEffects)
                            );
                            entry.setTriggeringCardId(spellCard.getId());
                            entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                            entry.setXValue(spellManaSpent);
                            gameData.stack.add(entry);
                        }

                        log.info("Game {} - {} graveyard spell-cast trigger queued",
                                gameData.id, card.getName());
                    }
                }
            }
        }

        // HAND_ON_CONTROLLER_CASTS_SPELL — hand-resident spell-cast triggers (Covercast: "Whenever you cast
        // another instant or sorcery spell, if five or more mana was spent to cast it, this card intensifies.").
        List<Card> castingPlayerHand = gameData.playerHands.get(castingPlayerId);
        if (castingPlayerHand != null) {
            for (Card card : new ArrayList<>(castingPlayerHand)) {
                if (card.getId().equals(spellCard.getId())) continue;
                for (CardEffect effect : SpellAbilityWatch.HAND_ON_CONTROLLER_CASTS_SPELL.effectsOf(card)) {
                    if (!(effect instanceof SpellCastTriggerEffect trigger)
                            || !predicateEvaluationService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)
                            || !spellManaSpentGatesMet(trigger, spellManaSpent)) {
                        continue;
                    }
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            castingPlayerId,
                            card.getName() + "'s ability",
                            new ArrayList<>(trigger.resolvedEffects())
                    );
                    entry.setTriggeringCardId(spellCard.getId());
                    entry.setXValue(spellManaSpent);
                    gameData.stack.add(entry);
                    log.info("Game {} - {} hand spell-cast trigger queued", gameData.id, card.getName());
                }
            }
        }

        // EXILE_ON_CONTROLLER_CASTS_SPELL — face-up exile-resident spell-cast triggers.
        for (ExiledCardEntry exiledEntry : new ArrayList<>(gameData.exiledCards)) {
            if (exiledEntry.faceDown() || !castingPlayerId.equals(exiledEntry.ownerId())) {
                continue;
            }
            Card card = exiledEntry.card();
            for (CardEffect effect : SpellAbilityWatch.EXILE_ON_CONTROLLER_CASTS_SPELL.effectsOf(card)) {
                registry.dispatch(new TriggerMatchContext(gameData, null, castingPlayerId, effect, card),
                        SpellAbilityWatch.EXILE_ON_CONTROLLER_CASTS_SPELL, effect, ctx);
            }
        }

        // Abaddon the Despoiler: during your turn, hand-cast spells with mana value at most the
        // total life lost by opponents this turn have cascade. The spell's effective mana value is
        // snapshotted so X spells use their announced value when the cascade trigger resolves.
        int opponentsLifeLostThisTurn = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(castingPlayerId))
                .mapToInt(playerId -> gameData.lifeLostThisTurn.getOrDefault(playerId, 0))
                .sum();
        if (castZone == Zone.HAND
                && castingPlayerId.equals(gameData.activePlayerId)
                && spellManaValue(gameData, spellCard) <= opponentsLifeLostThisTurn) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
                    if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()
                            || gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_HAND_SPELLS_WITH_MANA_VALUE_AT_MOST_OPPONENTS_LIFE_LOST);
                    if (grantEffects.isEmpty()) continue;

                    StackEntry cascadeTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            spellCard.getName() + "'s ability",
                            new ArrayList<>(grantEffects));
                    cascadeTrigger.setTriggeringCardId(spellCard.getId());
                    cascadeTrigger.setEventValue(spellManaValue(gameData, spellCard));
                    gameData.stack.add(cascadeTrigger);
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to {} for {} (opponents lost {} life)",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId,
                            opponentsLifeLostThisTurn);
                }
            }
        }

        // Sloppity Bilepiper: the next creature spell this turn has cascade. Each granted instance of
        // cascade is its own triggered ability (CR 702.85c), so each goes on the stack separately. The
        // spell's effective mana value is snapshotted so the existing CascadeEffect handler uses it.
        int pendingCreatureSpellCascades = gameData.consumeNextCreatureSpellCascade(
                castingPlayerId, spellCard);
        for (int i = 0; i < pendingCreatureSpellCascades; i++) {
            StackEntry cascadeTrigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    spellCard,
                    castingPlayerId,
                    spellCard.getName() + "'s ability",
                    new ArrayList<>(List.of(new CascadeEffect())));
            cascadeTrigger.setTriggeringCardId(spellCard.getId());
            cascadeTrigger.setEventValue(spellManaValue(gameData, spellCard));
            gameData.stack.add(cascadeTrigger);
            gameLogService.append(gameData, GameLog.cardThen(
                    spellCard, " has cascade from Sloppity Bilepiper."));
        }
        if (pendingCreatureSpellCascades > 0) {
            log.info("Game {} - queued {} delayed cascade grant(s) for {}",
                    gameData.id, pendingCreatureSpellCascades, spellCard.getName());
        }

        // "When you next cast a creature spell this turn, ..." delayed triggers (Yuna, Grand Summoner):
        // each goes on the stack above the spell as its own triggered ability.
        for (DelayedSpellCastTrigger delayed : gameData.consumeNextCreatureSpellDelayedTriggers(
                castingPlayerId, spellCard)) {
            StackEntry delayedTrigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.source(),
                    castingPlayerId,
                    delayed.source().getName() + "'s ability",
                    new ArrayList<>(List.of(delayed.effect())));
            delayedTrigger.setTriggeringCardId(spellCard.getId());
            gameData.stack.add(delayedTrigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(delayed.source()));
            log.info("Game {} - delayed cast trigger of {} queued for {}",
                    gameData.id, delayed.source().getName(), spellCard.getName());
        }

        // COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL — Eminence and similar command-zone spell-cast triggers
        List<Card> castingPlayerCommandZone = gameData.playerCommandZones.get(castingPlayerId);
        if (castingPlayerCommandZone != null) {
            for (Card card : new ArrayList<>(castingPlayerCommandZone)) {
                List<CardEffect> commandEffects = SpellAbilityWatch.COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL.effectsOf(card);
                if (commandEffects == null || commandEffects.isEmpty()) continue;

                for (CardEffect effect : commandEffects) {
                    if (effect instanceof SpellCastTriggerEffect trigger) {
                        if (!predicateEvaluationService.matchesCardPredicate(spellCard, trigger.spellFilter(), null)) {
                            continue;
                        }
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                castingPlayerId,
                                card.getName() + "'s ability",
                                new ArrayList<>(trigger.resolvedEffects())
                        ));
                        log.info("Game {} - {} command-zone spell-cast trigger queued",
                                gameData.id, card.getName());
                    }
                }
            }
        }

        ManaPool spellPaymentPool = gameData.playerManaPools.get(castingPlayerId);
        if (spellPaymentPool != null) {
            boolean instantOrSorcery = spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY);
            boolean red = spellCard.getColors() != null && spellCard.getColors().contains(CardColor.RED);
            int wellspringCopies = spellPaymentPool.takeSpentCopyGrantingMana(ManaCopyRider.INSTANT_OR_SORCERY_SPELL);
            int gogglesCopies = spellPaymentPool.takeSpentCopyGrantingMana(ManaCopyRider.RED_INSTANT_OR_SORCERY_SPELL);
            if (instantOrSorcery) {
                queueManaLinkedSpellCopyTriggers(gameData, spellCard, castingPlayerId, wellspringCopies);
            }
            if (instantOrSorcery && red) {
                queueManaLinkedSpellCopyTriggers(gameData, spellCard, castingPlayerId, gogglesCopies);
            }
        }

        // "When you next cast an instant or sorcery spell this turn, copy that spell"
        // (e.g. Chandra, the Firebrand −2). Same one-shot shape as Primal Wellspring's trigger, but
        // tracked in the turn-scoped counter so it survives mana drain.
        Integer pendingTurnCopies = gameData.pendingNextInstantSorceryCopyThisTurnCount.get(castingPlayerId);
        List<Integer> pendingMaxManaValues =
                gameData.pendingNextInstantSorceryCopyThisTurnMaxManaValues.get(castingPlayerId);
        List<DynamicAmount> pendingDynamicCounts =
                gameData.pendingNextInstantSorceryCopyThisTurnDynamicCounts.get(castingPlayerId);
        if ((pendingTurnCopies != null && pendingTurnCopies > 0
                || pendingMaxManaValues != null && !pendingMaxManaValues.isEmpty()
                || pendingDynamicCounts != null && !pendingDynamicCounts.isEmpty())
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                int limitedCopies = 0;
                if (pendingMaxManaValues != null) {
                    int manaValue = spellCard.getManaValue();
                    if (spellCard.getParsedManaCost() != null) {
                        manaValue += spellEntry.getXValue() * spellCard.getParsedManaCost().getXSymbolCount();
                    }
                    for (int i = pendingMaxManaValues.size() - 1; i >= 0; i--) {
                        if (manaValue <= pendingMaxManaValues.get(i)) {
                            pendingMaxManaValues.remove(i);
                            limitedCopies++;
                        }
                    }
                    if (pendingMaxManaValues.isEmpty()) {
                        gameData.pendingNextInstantSorceryCopyThisTurnMaxManaValues.remove(castingPlayerId);
                    }
                }
                int unrestrictedCopies = pendingTurnCopies == null ? 0 : pendingTurnCopies;
                if (pendingDynamicCounts != null) {
                    List<Card> dynamicSources = gameData.pendingNextInstantSorceryCopyThisTurnDynamicSources
                            .getOrDefault(castingPlayerId, List.of());
                    for (int dynamicIndex = 0; dynamicIndex < pendingDynamicCounts.size(); dynamicIndex++) {
                        DynamicAmount dynamicCount = pendingDynamicCounts.get(dynamicIndex);
                        Card triggerSource = dynamicIndex < dynamicSources.size()
                                && dynamicSources.get(dynamicIndex) != null
                                ? dynamicSources.get(dynamicIndex) : spellCard;
                        // The delayed trigger always triggers; how many copies it makes is counted as it resolves.
                        gameData.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                                triggerSource, castingPlayerId, "Copy " + spellCard.getName(),
                                List.of(new CopyControllerCastSpellEffect(
                                        new StackEntry(spellEntry), castingPlayerId, dynamicCount))));
                    }
                    gameData.pendingNextInstantSorceryCopyThisTurnDynamicCounts.remove(castingPlayerId);
                    gameData.pendingNextInstantSorceryCopyThisTurnDynamicSources.remove(castingPlayerId);
                }
                int totalCopies = unrestrictedCopies + limitedCopies;
                if (totalCopies > 0) {
                    StackEntry snapshot = new StackEntry(spellEntry);
                    Card countSource = unrestrictedCopies > 0
                            ? gameData.pendingNextInstantSorceryCopyThisTurnCountSource
                            .getOrDefault(castingPlayerId, spellCard) : spellCard;
                    // Each one-copy delayed trigger (Teach by Example) triggers separately (CR 603.7); the
                    // remaining unrestricted copies belong to a single multi-copy trigger ("copy it X times").
                    int separateTriggers = Math.min(unrestrictedCopies, Math.max(0,
                            gameData.pendingNextInstantSorceryCopyThisTurnSingleTriggerCount
                                    .getOrDefault(castingPlayerId, 0)));
                    for (int copyGroup = 0; copyGroup < 2; copyGroup++) {
                        int copies = copyGroup == 0 ? unrestrictedCopies - separateTriggers : limitedCopies;
                        if (copies > 0) {
                            gameData.stack.add(new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    copyGroup == 0 ? countSource : spellCard, castingPlayerId,
                                    "Copy " + spellCard.getName(),
                                    List.of(new CopyControllerCastSpellEffect(snapshot, castingPlayerId, copies))));
                        }
                        if (copyGroup == 0) {
                            for (int i = 0; i < separateTriggers; i++) {
                                gameData.stack.add(new StackEntry(
                                        StackEntryType.TRIGGERED_ABILITY, countSource, castingPlayerId,
                                        "Copy " + spellCard.getName(),
                                        List.of(new CopyControllerCastSpellEffect(snapshot, castingPlayerId, 1))));
                            }
                        }
                    }
                    if (unrestrictedCopies > 0) {
                        gameData.pendingNextInstantSorceryCopyThisTurnCount.remove(castingPlayerId);
                        gameData.pendingNextInstantSorceryCopyThisTurnSingleTriggerCount.remove(castingPlayerId);
                        gameData.pendingNextInstantSorceryCopyThisTurnCountSource.remove(castingPlayerId);
                        if (gameData.pendingNextSpellOrLoyaltyCopyLinkedThisTurn.remove(castingPlayerId)) {
                            gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.remove(castingPlayerId);
                            gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn.remove(castingPlayerId);
                        }
                    }
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                    log.info("Game {} - {} delayed spell-copy trigger(s) queued for {}",
                            gameData.id, totalCopies, spellCard.getName());
                }
            }
        }

        // "The next instant or sorcery spell you cast this turn has Storm" (Storm, Force of Nature).
        Integer pendingStorms = gameData.pendingNextInstantSorceryStormThisTurnCount.get(castingPlayerId);
        if (pendingStorms != null && pendingStorms > 0
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int i = 0; i < pendingStorms; i++) {
                    queueStormTrigger(gameData, spellCard, castingPlayerId, spellEntry, false, false);
                }
                gameData.pendingNextInstantSorceryStormThisTurnCount.remove(castingPlayerId);
                log.info("Game {} - {} delayed Storm grant(s) applied to {}",
                        gameData.id, pendingStorms, spellCard.getName());
            }
        }

        Integer pendingSpellCopies = gameData.pendingNextSpellCopyThisTurnCount.get(castingPlayerId);
        if (pendingSpellCopies != null && pendingSpellCopies > 0) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                boolean permanentSpell = switch (spellEntry.getEntryType()) {
                    case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
                    default -> false;
                };
                StackEntry snapshot = new StackEntry(spellEntry);
                List<CardEffect> copyEffects = new ArrayList<>(pendingSpellCopies);
                for (int i = 0; i < pendingSpellCopies; i++) {
                    copyEffects.add(new CopyControllerCastSpellEffect(
                            snapshot, castingPlayerId, Set.of(), Set.of(), permanentSpell, true));
                }
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        "Copy " + spellCard.getName(),
                        copyEffects
                ));
                gameData.pendingNextSpellCopyThisTurnCount.remove(castingPlayerId);
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                log.info("Game {} - {} all-spell copy trigger(s) queued for {}",
                        gameData.id, pendingSpellCopies, spellCard.getName());
            }
        }

        List<CopyNextSpellCastThisTurnEffect> pendingFilteredCopies =
                gameData.pendingNextFilteredSpellCopiesThisTurn.get(castingPlayerId);
        if (pendingFilteredCopies != null && !pendingFilteredCopies.isEmpty()) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                Card spellOnStack = SpellCastTriggerCollectorService.spellOnStackWithChosenX(
                        spellCard, spellEntry.getXValue());
                List<CopyNextSpellCastThisTurnEffect> matchingCopies = new ArrayList<>();
                List<CopyNextSpellCastThisTurnEffect> remainingCopies = new ArrayList<>();
                for (CopyNextSpellCastThisTurnEffect copyEffect : pendingFilteredCopies) {
                    // "{X} in its mana cost" is read off the printed cost, which the chosen X is substituted into
                    // on the stack.
                    Card filterSubject = copyEffect.spellFilter() instanceof CardHasXInManaCostPredicate
                            ? spellCard : spellOnStack;
                    if (copyEffect.spellFilter() == null
                            || predicateEvaluationService.matchesCardPredicate(
                            filterSubject, copyEffect.spellFilter(), null, gameData, castingPlayerId)) {
                        matchingCopies.add(copyEffect);
                    } else {
                        remainingCopies.add(copyEffect);
                    }
                }
                if (!matchingCopies.isEmpty()) {
                    boolean permanentSpell = switch (spellEntry.getEntryType()) {
                        case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
                        default -> false;
                    };
                    StackEntry snapshot = new StackEntry(spellEntry);
                    List<CardEffect> copyEffects = new ArrayList<>(matchingCopies.size());
                    for (CopyNextSpellCastThisTurnEffect copyEffect : matchingCopies) {
                        copyEffects.add(new CopyControllerCastSpellEffect(
                                snapshot, castingPlayerId, Set.of(), Set.of(),
                                copyEffect.removedSupertypes(), permanentSpell, true));
                    }
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName(),
                            copyEffects
                    ));
                    int linkedXCopies = (int) matchingCopies.stream()
                            .filter(copyEffect -> copyEffect.spellFilter() instanceof CardHasXInManaCostPredicate)
                            .count();
                    if (linkedXCopies > 0) {
                        consumeLinkedXAbilityCopies(gameData, castingPlayerId, linkedXCopies);
                    }
                    if (remainingCopies.isEmpty()) {
                        gameData.pendingNextFilteredSpellCopiesThisTurn.remove(castingPlayerId);
                    } else {
                        gameData.pendingNextFilteredSpellCopiesThisTurn.put(castingPlayerId, remainingCopies);
                    }
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                    log.info("Game {} - {} filtered spell-copy trigger(s) queued for {}",
                            gameData.id, matchingCopies.size(), spellCard.getName());
                }
            }
        }

        // "Until end of turn, whenever you cast an instant or sorcery spell, copy it"
        // (e.g. The Mirari Conjecture chapter III). Each granting ability triggers on its own.
        int spellCopyGrants = gameData.spellCopyGrantsUntilEndOfTurn.getOrDefault(castingPlayerId, 0);
        if (spellCopyGrants > 0
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            // Find the spell on the stack to create a snapshot
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int grant = 0; grant < spellCopyGrants; grant++) {
                    StackEntry snapshot = new StackEntry(spellEntry);
                    CopyControllerCastSpellEffect copyEffect =
                            new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName(),
                            new ArrayList<>(List.of(copyEffect))
                    ));
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (The Mirari Conjecture)."));
                    log.info("Game {} - {} spell-copy trigger queued for {}",
                            gameData.id, spellCard.getName(), castingPlayerId);
                }
            }
        }

        // Conspire (CR 702.78): "When you [tap the two creatures], copy it and you may choose a new
        // target for the copy." Each conspire instance paid during casting queues its own copy
        // trigger; the count is recorded in gameData.conspiredSpellCopyCounts.
        Integer conspireCopies = gameData.conspiredSpellCopyCounts.remove(spellCard.getId());
        if (conspireCopies != null && conspireCopies > 0) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int i = 0; i < conspireCopies; i++) {
                    StackEntry snapshot = new StackEntry(spellEntry);
                    CopyControllerCastSpellEffect copyEffect =
                            new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName() + " (Conspire)",
                            new ArrayList<>(List.of(copyEffect))
                    ));
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (Conspire)."));
                    log.info("Game {} - {} conspire copy trigger queued for {}",
                            gameData.id, spellCard.getName(), castingPlayerId);
                }
            }
        }

        Integer casualtyCopies = gameData.casualtySpellCopyCounts.remove(spellCard.getId());
        if (casualtyCopies != null && casualtyCopies > 0) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int copyNumber = 0; copyNumber < casualtyCopies; copyNumber++) {
                    CopyControllerCastSpellEffect copyEffect =
                            new CopyControllerCastSpellEffect(new StackEntry(spellEntry), castingPlayerId);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName() + " (Casualty)",
                            new ArrayList<>(List.of(copyEffect))
                    ));
                }
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (Casualty)."));
                log.info("Game {} - {} casualty copy trigger(s) queued for {}",
                        gameData.id, casualtyCopies, spellCard.getName());
            }
        }

        // ON_SELF_CAST — "When you cast this spell, ..." triggers scanned against the just-cast card
        // itself (it's a spell on the stack, not a permanent). CopyThisSpellIfConditionEffect (SOS
        // Infusion copy cycle) needs a snapshot of the spell entry; any other effect (e.g. Demigod of
        // Revenge's graveyard return) is queued as a plain triggered ability under the caster.
        List<CardEffect> selfCastTriggeredEffects = new ArrayList<>();
        List<CardEffect> selfCastEffects = new ArrayList<>();
        if (spellCard.getManaCost() != null) {
            gameQueryService.getSpellCastingAbilityGrantValues(
                            gameData, castingPlayerId, spellCard, Keyword.REPLICATE, castZone).stream()
                    .map(value -> value > 0 ? "{" + value + "}" : spellCard.getManaCost())
                    .distinct()
                    .filter(replicateCost -> SpellAbilityWatch.ON_SELF_CAST.effectsOf(spellCard).stream()
                            .noneMatch(effect -> effect instanceof ReplicateEffect replicate
                                    && replicateCost.equals(replicate.manaCost())))
                    .map(ReplicateEffect::new)
                    .forEach(selfCastEffects::add);
        }

        ManaPool castingPlayerPool = gameData.playerManaPools.get(castingPlayerId);
        int pendingSpellOrAbilityCopies = castingPlayerPool == null ? 0
                : castingPlayerPool.takeSpentCopyGrantingMana(ManaCopyRider.SPELL_OR_ABILITY);
        if (pendingSpellOrAbilityCopies > 0) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                StackEntry snapshot = new StackEntry(spellEntry);
                boolean permanentSpell = switch (spellEntry.getEntryType()) {
                    case CREATURE_SPELL, ARTIFACT_SPELL, ENCHANTMENT_SPELL, PLANESWALKER_SPELL,
                            BATTLE_SPELL -> true;
                    default -> false;
                };
                List<CardEffect> copyEffects = new ArrayList<>(pendingSpellOrAbilityCopies);
                for (int i = 0; i < pendingSpellOrAbilityCopies; i++) {
                    copyEffects.add(new CopyControllerCastSpellEffect(
                            snapshot, castingPlayerId, Set.of(), Set.of(), permanentSpell, true));
                }
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        "Copy " + spellCard.getName(),
                        copyEffects
                ));
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                log.info("Game {} - {} mana-linked spell-or-ability copy trigger(s) queued for {}",
                        gameData.id, pendingSpellOrAbilityCopies, spellCard.getName());
            }
        }
        // Each granted instance of demonstrate triggers separately (e.g. two Silverquill Lecturers).
        gameQueryService.getSpellCastingAbilityGrantValues(
                        gameData, castingPlayerId, spellCard, Keyword.DEMONSTRATE, castZone)
                .forEach(grant -> selfCastEffects.add(new MayEffect(
                        new DemonstrateEffect(), "Copy " + spellCard.getName() + "?")));
        selfCastEffects.addAll(SpellAbilityWatch.ON_SELF_CAST.effectsOf(spellCard));
        List<CardEffect> perpetualSelfCastEffects =
                gameData.perpetualTriggerEffects(spellCard.getId(),
                        SpellAbilityWatch.ON_SELF_CAST.trigger(), SpellAbilityWatch.ON_SELF_CAST.zone());
        if (!perpetualSelfCastEffects.isEmpty()) {
            for (CardEffect grantedAbility : perpetualSelfCastEffects) {
                if (!grantedAbility.targetSpec().equals(com.github.laxika.magicalvibes.model.effect.TargetSpec.NONE)
                        || grantedAbility instanceof CopyThisSpellIfConditionEffect) {
                    selfCastEffects.add(grantedAbility);
                    continue;
                }
                StackEntry grantedTrigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, spellCard,
                        castingPlayerId, spellCard.getName() + "'s ability", List.of(grantedAbility));
                grantedTrigger.setSourceZone(castZone);
                grantedTrigger.setTriggeringCardId(spellCard.getId());
                grantedTrigger.setNonTargeting(true);
                gameData.stack.add(grantedTrigger);
            }
        }
        StackEntry selfCastSpellEntry = gameData.stack.stream()
                .filter(entry -> entry.getTargetableId().equals(spellCard.getId()))
                .findFirst()
                .orElse(null);
        for (CardEffect effect : selfCastEffects) {
            if (selfCastSpellEntry != null && effect instanceof MayEffect may
                    && may.wrapped() instanceof DemonstrateEffect demonstrate
                    && demonstrate.spellSnapshot() == null) {
                effect = new MayEffect(new DemonstrateEffect(new StackEntry(selfCastSpellEntry)),
                        may.prompt(), may.elseEffect(), may.choicePlayer());
            } else if (selfCastSpellEntry != null && effect instanceof DemonstrateEffect demonstrate
                    && demonstrate.spellSnapshot() == null) {
                effect = new DemonstrateEffect(new StackEntry(selfCastSpellEntry));
            }
            if (effect instanceof ExileTriggeringSpellWithSuspendCountersEffect suspend
                    && suspend.skipIfCastFromExile() && castZone == Zone.EXILE) {
                continue;
            }
            if (effect instanceof ConditionalEffect conditional
                    && conditional.interveningIf()
                    && selfCastSpellEntry != null
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forStackEntry(selfCastSpellEntry))) {
                continue;
            }
            if (effect instanceof ConditionalEffect conditional
                    && conditional.wrapped() instanceof CopyThisSpellIfConditionEffect) {
                effect = conditional.wrapped();
            }
            if (effect instanceof CopyThisSpellIfConditionEffect trigger) {
                StackEntry spellEntry = null;
                for (StackEntry se : gameData.stack) {
                    if (se.getTargetableId().equals(spellCard.getId())) {
                        spellEntry = se;
                        break;
                    }
                }
                if (spellEntry == null) continue;

                StackEntry snapshot = new StackEntry(spellEntry);
                List<CardEffect> copies = new ArrayList<>();
                for (int i = 0; i < trigger.copies(); i++) {
                    copies.add(new CopyControllerCastSpellEffect(snapshot, castingPlayerId,
                            Set.of(), Set.of(), trigger.tokenCopy()));
                }
                CardEffect copyEffect = copies.size() == 1 ? copies.getFirst()
                        : new SequenceEffect(copies);
                if (trigger.conditionAtCast()) {
                    if (!conditionEvaluationService.isMet(gameData, trigger.condition(),
                            ConditionContext.forStackEntry(spellEntry))) {
                        continue;
                    }
                    if (trigger.optional()) {
                        copyEffect = new MayEffect(copyEffect, "Copy " + spellCard.getName() + "?");
                    }
                } else {
                    if (trigger.optional()) {
                        copyEffect = new MayEffect(copyEffect, "Copy " + spellCard.getName() + "?");
                    }
                    copyEffect = new ConditionalEffect(trigger.condition(), copyEffect);
                }
                StackEntry copyTrigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        spellCard.getName() + "'s ability",
                        new ArrayList<>(List.of(copyEffect))
                );
                copyTrigger.setSourceZone(castZone);
                gameData.stack.add(copyTrigger);
                log.info("Game {} - {} self-cast copy trigger queued for {}",
                        gameData.id, spellCard.getName(), castingPlayerId);
            } else if (effect instanceof CopyThisSpellIfCasualtyPaidEffect casualtyTrigger) {
                StackEntry spellEntry = null;
                for (StackEntry se : gameData.stack) {
                    if (se.getTargetableId().equals(spellCard.getId())) {
                        spellEntry = se;
                        break;
                    }
                }
                if (spellEntry == null || !spellEntry.isCasualtyCostPaid()) continue;

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        spellCard.getName() + "'s ability",
                        new ArrayList<>(List.of(new CopyControllerCastSpellEffect(
                                new StackEntry(spellEntry), castingPlayerId, Set.of(), Set.of(),
                                casualtyTrigger.removedSupertypes(), casualtyTrigger.tokenCopy(), true, false, false,
                                casualtyTrigger.startingLoyaltyFromX())))
                ));
                log.info("Game {} - {} casualty copy trigger queued for {}",
                        gameData.id, spellCard.getName(), castingPlayerId);
            } else if (effect instanceof CopyThisSpellForXValueEffect) {
                StackEntry spellEntry = null;
                for (StackEntry se : gameData.stack) {
                    if (se.getTargetableId().equals(spellCard.getId())) {
                        spellEntry = se;
                        break;
                    }
                }
                if (spellEntry == null) continue;

                int copies = Math.max(0, spellEntry.getXValue());
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        spellCard.getName() + "'s ability",
                        new ArrayList<>(List.of(new StormCopyEffect(
                                new StackEntry(spellEntry), castingPlayerId, copies, false)))
                ));
                log.info("Game {} - {} self-cast trigger queued ({} copies) for {}",
                        gameData.id, spellCard.getName(), copies, castingPlayerId);
            } else if (effect instanceof ReplicateEffect replicate) {
                StackEntry spellEntry = null;
                for (StackEntry se : gameData.stack) {
                    if (se.getTargetableId().equals(spellCard.getId())) {
                        spellEntry = se;
                        break;
                    }
                }
                if (spellEntry == null) continue;

                int copies = replicate.tapFilter() != null
                        ? Math.max(0, spellEntry.getXValue())
                        : (int) spellEntry.getRepeatedAdditionalCosts().stream()
                                .filter(replicate.manaCost()::equals)
                                .count();
                if (copies <= 0) continue;

                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        spellCard.getName() + "'s ability",
                        new ArrayList<>(List.of(new StormCopyEffect(
                                new StackEntry(spellEntry), castingPlayerId, copies, replicate.tokenCopy())))
                ));
                log.info("Game {} - {} replicate trigger queued ({} copies) for {}",
                        gameData.id, spellCard.getName(), copies, castingPlayerId);
            } else if (effect instanceof SpellCastCopyTriggerEffect copyTrigger) {
                StackEntry spellEntry = null;
                for (StackEntry se : gameData.stack) {
                    if (se.getTargetableId().equals(spellCard.getId())) {
                        spellEntry = se;
                        break;
                    }
                }
                if (spellEntry == null) continue;

                int copies = copyTrigger.copyCount(gameData, castingPlayerId);
                StackEntry snapshot = new StackEntry(spellEntry);
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        spellCard,
                        castingPlayerId,
                        spellCard.getName() + "'s ability",
                        new ArrayList<>(List.of(new StormCopyEffect(
                                snapshot, castingPlayerId, copies, copyTrigger.tokenCopy(), false,
                                copyTrigger instanceof com.github.laxika.magicalvibes.model.effect.GravestormEffect
                                        || copyTrigger instanceof com.github.laxika.magicalvibes.model.effect.CoststormEffect
                                        || copyTrigger instanceof com.github.laxika.magicalvibes.model.effect.CopyThisSpellForEachCommanderCastEffect
                                        || copyTrigger instanceof com.github.laxika.magicalvibes.model.effect.CopyThisSpellForEachCounterKindEffect
                                        ? copyTrigger : null)))
                ));
                log.info("Game {} - {} spell-copy trigger queued ({} copies) for {}",
                        gameData.id, spellCard.getName(), copies, castingPlayerId);
            } else if (effect instanceof StormEffect storm) {
                StackEntry spellEntry = null;
                for (StackEntry se : gameData.stack) {
                    if (se.getTargetableId().equals(spellCard.getId())) {
                        spellEntry = se;
                        break;
                    }
                }
                if (spellEntry == null) continue;

                // "for each spell cast before it this turn" — this spell is already recorded, so
                // subtract it out. Count is fixed here (spells cast after can't precede this one).
                queueStormTrigger(gameData, spellCard, castingPlayerId, spellEntry,
                        storm.instantOrSorceryOnly(), storm.tokenCopy(), storm.removeLegendary());
            } else if (effect instanceof CascadeEffect) {
                StackEntry cascadeTrigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                        spellCard, castingPlayerId, spellCard.getName() + "'s cascade ability",
                        new ArrayList<>(List.of(effect)),
                        selfCastSpellEntry == null ? 0 : selfCastSpellEntry.getXValue());
                cascadeTrigger.setKicked(selfCastSpellEntry != null && selfCastSpellEntry.wasKicked());
                cascadeTrigger.setSourceZone(castZone);
                gameData.stack.add(cascadeTrigger);
            } else if (DemonstrateEffect.isWrappedBy(effect)) {
                // CR 702.144a / CR 603.5 — demonstrate goes on the stack when the spell is cast;
                // whether to copy is decided when the trigger resolves.
                StackEntry demonstrateTrigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                        spellCard, castingPlayerId, spellCard.getName() + "'s demonstrate ability",
                        new ArrayList<>(List.of(effect)));
                demonstrateTrigger.setSourceZone(castZone);
                demonstrateTrigger.setTriggeringCardId(spellCard.getId());
                demonstrateTrigger.setNonTargeting(true);
                gameData.stack.add(demonstrateTrigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(spellCard));
            } else {
                selfCastTriggeredEffects.add(effect);
            }
        }
        if (!selfCastTriggeredEffects.isEmpty()) {
            if (selfCastTriggeredEffects.size() == 1
                    && selfCastTriggeredEffects.getFirst() instanceof MayEffect may
                    && may.targetSpec() == TargetSpec.NONE
                    && !(may.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.RippleEffect)) {
                gameData.pendingMayAbilities.add(PendingMayAbility.forSpellCastTrigger(
                        spellCard,
                        castingPlayerId,
                        new ArrayList<>(List.of(may.wrapped())),
                        spellCard.getName() + " — " + may.prompt(),
                        null,
                        null,
                        spellCard.getId()));
                log.info("Game {} - {} self-cast may trigger queued for {}",
                        gameData.id, spellCard.getName(), castingPlayerId);
            } else {
                // Targeted ON_SELF_CAST (e.g. Abundant Maw: "target opponent loses 3 life") chooses
                // targets as the ability goes on the stack — reuse SpellTargetTriggerAnyTarget.
                // Multi-target ("up to N target permanents", Elder Deep-Fiend) reuses the ETB
                // multi-target slot walker with a null source permanent id.
                boolean needsPlayerTarget = selfCastTriggeredEffects.stream()
                        .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                boolean needsPermanentTarget = selfCastTriggeredEffects.stream()
                        .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
                boolean needsGraveyardTarget = selfCastTriggeredEffects.stream()
                        .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
                if (needsGraveyardTarget && !needsPlayerTarget && !needsPermanentTarget) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                            spellCard, castingPlayerId, new ArrayList<>(selfCastTriggeredEffects)));
                    log.info("Game {} - {} self-cast graveyard-target trigger queued for {}",
                            gameData.id, spellCard.getName(), castingPlayerId);
                } else if (needsPlayerTarget || needsPermanentTarget) {
                    boolean multiTarget = spellCard.getTargetGroups().size() > 1
                            || etbTokenTargetService.needsSlotBySlotTargetSelection(spellCard);
                    if (multiTarget) {
                        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                spellCard, castingPlayerId, new ArrayList<>(selfCastTriggeredEffects),
                                null, new ArrayList<>(), 0, 0));
                        log.info("Game {} - {} self-cast multi-target trigger queued for {}",
                                gameData.id, spellCard.getName(), castingPlayerId);
                    } else {
                        boolean playerTargetOnly = needsPlayerTarget && !needsPermanentTarget;
                        boolean optionalTarget = spellCard.getTargetGroups().size() == 1
                                && spellCard.getTargetGroups().getFirst().getMinTargets() == 0
                                && spellCard.getTargetGroups().getFirst().getMaxTargets() == 1
                                && selfCastTriggeredEffects.stream().anyMatch(effect ->
                                spellCard.getEffectTargetIndex(effect)
                                        == spellCard.getTargetGroups().getFirst().getIndex());
                        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                                spellCard, castingPlayerId, new ArrayList<>(selfCastTriggeredEffects),
                                playerTargetOnly, spellCard.getTargetFilter(), 0, null, optionalTarget));
                        log.info("Game {} - {} self-cast targeting trigger queued for {}",
                                gameData.id, spellCard.getName(), castingPlayerId);
                    }
                } else {
                    // Carry the spell's X onto the trigger so "reveal the top X cards" (Genesis Hydra)
                    // sees the value locked in on cast (CR 601.2b); 0 for spells without {X}.
                    int selfCastX = 0;
                    boolean selfCastKicked = false;
                    for (StackEntry se : gameData.stack) {
                        if (se.getTargetableId().equals(spellCard.getId())) {
                            selfCastX = se.getXValue();
                            selfCastKicked = se.wasKicked();
                            break;
                        }
                    }
                    StackEntry selfCastTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            spellCard.getName() + "'s ability",
                            new ArrayList<>(selfCastTriggeredEffects),
                            selfCastX
                    );
                    selfCastTrigger.setKicked(selfCastKicked);
                    selfCastTrigger.setSourceZone(castZone);
                    if (selfCastTriggeredEffects.stream()
                            .anyMatch(TriggeringSpellReferencingEffect.class::isInstance)) {
                        selfCastTrigger.setTriggeringCardId(spellCard.getId());
                    }
                    gameData.stack.add(selfCastTrigger);
                    log.info("Game {} - {} self-cast trigger queued for {}",
                            gameData.id, spellCard.getName(), castingPlayerId);
                }
            }
        }

        if (castZone == Zone.HAND && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
                    if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()
                            || gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_INSTANT_OR_SORCERY_FROM_HAND);
                    if (grantEffects.isEmpty()) continue;

                    StackEntry cascadeTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            spellCard.getName() + "'s ability",
                            new ArrayList<>(grantEffects)
                    );
                    cascadeTrigger.setTriggeringCardId(spellCard.getId());
                    cascadeTrigger.setEventValue(spellManaValue(gameData, spellCard));
                    gameData.stack.add(cascadeTrigger);
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to {} cast from hand for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }
        // COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL — Eminence and similar command-zone spell-cast triggers

        if (castZone == Zone.HAND && spellCard.hasType(CardType.ENCHANTMENT)) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
                    if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()
                            || gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_ENCHANTMENT_FROM_HAND);
                    if (grantEffects.isEmpty()) continue;

                    StackEntry cascadeTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            spellCard.getName() + "'s ability",
                            new ArrayList<>(grantEffects));
                    cascadeTrigger.setTriggeringCardId(spellCard.getId());
                    cascadeTrigger.setEventValue(spellManaValue(gameData, spellCard));
                    gameData.stack.add(cascadeTrigger);
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to enchantment {} cast from hand for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        // "The first spell you cast each turn has cascade" (Maelstrom Nexus). A permanent-granted
        // keyword: a static GrantCastTriggerToSpellsEffect on the caster's battlefield. recordSpellCast runs before this method in
        // every cast path, so a count of 1 identifies the caster's first spell of the turn. The held
        // CascadeEffect is queued keyed to the just-cast spell so CascadeEffectHandler's threshold is
        // the spell's mana value (not the granting permanent's). One trigger per granting permanent.
        if (gameQueryService.cardHasSubtype(spellCard, CardSubtype.SLIVER, gameData, castingPlayerId)) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_SLIVER_SPELL);
                    if (grantEffects.isEmpty()) continue;

                    StackEntry grantedCascade = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            spellCard.getName() + "'s ability",
                            new ArrayList<>(grantEffects));
                    StackEntry grantedCascadeSpell = findStackEntryByCardId(gameData, spellCard.getId());
                    if (grantedCascadeSpell != null) {
                        grantedCascade.setXValue(grantedCascadeSpell.getXValue());
                    }
                    gameData.stack.add(grantedCascade);
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to Sliver spell {} for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        if (gameData.getSpellsCastThisTurnCount(castingPlayerId) == 1) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_FIRST_SPELL);
                    if (grantEffects.isEmpty()) continue;

                    StackEntry grantedCascade = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            spellCard.getName() + "'s ability",
                            new ArrayList<>(grantEffects));
                    StackEntry grantedCascadeSpell = findStackEntryByCardId(gameData, spellCard.getId());
                    if (grantedCascadeSpell != null) {
                        grantedCascade.setXValue(grantedCascadeSpell.getXValue());
                    }
                    gameData.stack.add(grantedCascade);
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to first spell {} for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        // "The first spell you cast each turn that mana from a Treasure was spent to cast has
        // cascade" (Rain of Riches). Unlike the ordinary first-spell marker above, a spell cast
        // without Treasure mana does not consume this grant.
        if (castUsingTreasureMana
                && gameData.getSpellsCastUsingTreasureManaThisTurnCount(castingPlayerId) == 1) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_FIRST_SPELL_USING_TREASURE_MANA);
                    if (grantEffects.isEmpty()) continue;

                    StackEntry grantedCascade = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            spellCard,
                            castingPlayerId,
                            spellCard.getName() + "'s ability",
                            new ArrayList<>(grantEffects));
                    StackEntry grantedCascadeSpell = findStackEntryByCardId(gameData, spellCard.getId());
                    if (grantedCascadeSpell != null) {
                        grantedCascade.setXValue(grantedCascadeSpell.getXValue());
                    }
                    gameData.stack.add(grantedCascade);
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to first Treasure-mana-funded spell {} for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        if (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY)) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()
                            || gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.STORM_TO_INSTANT_OR_SORCERY);
                    if (grantEffects.isEmpty()) continue;

                    StackEntry spellEntry = null;
                    for (StackEntry se : gameData.stack) {
                        if (se.getTargetableId().equals(spellCard.getId())) {
                            spellEntry = se;
                            break;
                        }
                    }
                    if (spellEntry == null) continue;

                    int copies = gameData.getTotalSpellsCastThisTurnCount() - 1;
                    StackEntry snapshot = new StackEntry(spellEntry);
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            castingPlayerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(new StormCopyEffect(snapshot, castingPlayerId, copies))),
                            0,
                            perm.getId()
                    ));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants Storm to {} ({} copies) for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), copies, castingPlayerId);
                }
            }
        }

        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * The triggered abilities a permanent's static {@link GrantCastTriggerToSpellsEffect}s of the given kind give
     * the spell being cast, in the order the permanent lists them.
     */
    private static List<CardEffect> castTriggerGrants(Permanent perm, SpellCastTriggerGrant grant) {
        return perm.getCard().getStaticEffects().stream()
                .filter(GrantCastTriggerToSpellsEffect.class::isInstance)
                .map(GrantCastTriggerToSpellsEffect.class::cast)
                .filter(granting -> granting.grant() == grant)
                .map(GrantCastTriggerToSpellsEffect::ability)
                .toList();
    }

    private void queueStormTrigger(GameData gameData, Card spellCard, UUID castingPlayerId,
                                   StackEntry spellEntry, boolean instantOrSorceryOnly, boolean tokenCopy) {
        queueStormTrigger(gameData, spellCard, castingPlayerId, spellEntry,
                instantOrSorceryOnly, tokenCopy, false);
    }

    private void queueStormTrigger(GameData gameData, Card spellCard, UUID castingPlayerId,
                                   StackEntry spellEntry, boolean instantOrSorceryOnly,
                                   boolean tokenCopy, boolean removeLegendary) {
        int copies = Math.max(0, instantOrSorceryOnly
                ? (int) gameData.getSpellsCastThisTurn(castingPlayerId).stream()
                        .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                        .count() - 1
                : gameData.getTotalSpellsCastThisTurnCount() - 1);
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                spellCard,
                castingPlayerId,
                spellCard.getName() + "'s ability",
                new ArrayList<>(List.of(new StormCopyEffect(
                        new StackEntry(spellEntry), castingPlayerId, copies, tokenCopy,
                        removeLegendary,
                        instantOrSorceryOnly ? new OtherInstantsAndSorceriesCastCountEffect() : null)))
        ));
        log.info("Game {} - {} Storm trigger queued ({} copies) for {}",
                gameData.id, spellCard.getName(), copies, castingPlayerId);
    }

    private int spellManaValue(GameData gameData, Card spellCard) {
        StackEntry spellEntry = findStackEntryByCardId(gameData, spellCard.getId());
        if (spellEntry != null && spellEntry.isCastFaceDown()) {
            return 0;
        }
        return spellCard.getManaValue() + (spellEntry == null || spellCard.getParsedManaCost() == null
                ? 0 : spellEntry.getXValue() * spellCard.getParsedManaCost().getXSymbolCount());
    }

    private void dispatchSuspendedExiledCardSpellCastTriggers(GameData gameData, UUID castingPlayerId,
            TriggerContext.SpellCast context) {
        for (ExiledCardEntry exiledEntry : new ArrayList<>(gameData.exiledCards)) {
            UUID ownerId = exiledEntry.ownerId();
            if (ownerId.equals(castingPlayerId)) {
                continue;
            }
            if (gameData.exiledCardsWithNonSuspendTimeCounters.contains(exiledEntry.card().getId())) {
                continue;
            }
            Integer timeCounters = gameData.exiledCardTimeCounters.get(exiledEntry.card().getId());
            if (timeCounters == null || timeCounters <= 0) {
                continue;
            }

            Card card = exiledEntry.card();
            for (CardEffect effect : SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL.effectsOf(card)) {
                registry.dispatch(new TriggerMatchContext(gameData, null, ownerId, effect, card),
                        SpellAbilityWatch.ON_OPPONENT_CASTS_SPELL, effect, context);
            }
        }
    }

    private boolean isNthMatchingSpell(GameData gameData, SpellCastTriggerEffect trigger, UUID playerId) {
        long matchingSpells = gameData.getSpellsCastThisTurn(playerId).stream()
                .filter(spell -> predicateEvaluationService.matchesCardPredicate(spell, trigger.spellFilter(), null))
                .count();
        return matchingSpells == trigger.nthSpellNumber();
    }

    private boolean isUnconditionalNonTargetingEmblemTrigger(SpellCastTriggerEffect trigger) {
        return trigger.spellFilter() == null
                && trigger.manaCost() == null
                && trigger.targetFilter() == null
                && trigger.castSpellTargetCondition() == null
                && !trigger.onlyDuringOpponentTurn()
                && !trigger.onlyDuringControllerTurn()
                && trigger.intervening() == null
                && trigger.nthSpellNumber() == 0
                && !trigger.resolvedEffects().isEmpty()
                && trigger.resolvedEffects().stream()
                .allMatch(effect -> effect.targetSpec().equals(TargetSpec.NONE));
    }

    /** Fires effects that care when the given player controls an effect that counters a spell. */
    private boolean isNonTargetingEmblemSpellCastTrigger(SpellCastTriggerEffect trigger) {
        return trigger.manaCost() == null
                && trigger.targetFilter() == null
                && trigger.castSpellTargetCondition() == null
                && !trigger.onlyDuringOpponentTurn()
                && !trigger.onlyDuringControllerTurn()
                && trigger.intervening() == null
                && !trigger.resolvedEffects().isEmpty()
                && trigger.resolvedEffects().stream()
                .allMatch(effect -> effect.targetSpec().equals(TargetSpec.NONE));
    }

    public void checkControllerCountersSpellTriggers(GameData gameData, UUID counteringPlayerId) {
        if (counteringPlayerId == null) return;

        var ctx = new TriggerContext.SpellCountered(counteringPlayerId);
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(counteringPlayerId)) return;
            dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_CONTROLLER_COUNTERS_SPELL, ctx);
        });
    }

    /** Fires effects that care when a spell cast by the given player is countered. */
    public void checkControllerSpellCounteredTriggers(GameData gameData, UUID spellControllerId) {
        if (spellControllerId == null) return;

        var ctx = new TriggerContext.SpellCastCountered(spellControllerId);
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(spellControllerId)) return;
            dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_CONTROLLER_SPELL_COUNTERED, ctx);
        });
    }

    // ── Discard triggers ───────────────────────────────────────────────

    /** Fires a spell's own trigger when it is countered or fizzles. */
    public void checkSelfSpellCounteredOrFizzledTriggers(GameData gameData, StackEntry spellEntry) {
        if (spellEntry == null || spellEntry.getCard() == null || spellEntry.getControllerId() == null
                || (spellEntry.getEntryType() != StackEntryType.INSTANT_SPELL
                && spellEntry.getEntryType() != StackEntryType.SORCERY_SPELL)) return;

        TriggerContext context = new TriggerContext.SpellCounteredOrFizzled(new StackEntry(spellEntry));
        Card spellCard = spellEntry.getCard();
        for (CardEffect effect : SpellAbilityWatch.ON_SELF_SPELL_COUNTERED_OR_FIZZLED.effectsOf(spellCard)) {
            dispatch(new TriggerMatchContext(gameData, null, spellEntry.getControllerId(), effect, spellCard),
                    SpellAbilityWatch.ON_SELF_SPELL_COUNTERED_OR_FIZZLED, effect, context);
        }
    }

    public void beginDiscardEvent(GameData gameData, UUID discardingPlayerId) {
        if (gameData.discardEventPlayerId == null) {
            gameData.discardEventPlayerId = discardingPlayerId;
            gameData.discardEventCardCount = 0;
            gameData.discardEventCards.clear();
        } else if (!gameData.discardEventPlayerId.equals(discardingPlayerId)) {
            throw new IllegalStateException("A discard event is already in progress for another player");
        }
    }

    public void finishDiscardEvent(GameData gameData) {
        UUID discardingPlayerId = gameData.discardEventPlayerId;
        int discardedCount = gameData.discardEventCardCount;
        List<Card> discardedCards = List.copyOf(gameData.discardEventCards);
        gameData.discardEventPlayerId = null;
        gameData.discardEventCardCount = 0;
        gameData.discardEventCards.clear();
        stateTriggerService.getObject().checkStateTriggers(gameData);
        if (discardingPlayerId != null && discardedCount > 0) {
            boolean triggered = checkDiscardEventTriggers(gameData, discardingPlayerId, discardedCount,
                    discardedCards);
            if (triggered) {
                gameOutcomeService.checkWinCondition(gameData);
            }
        }
    }

    public void checkDiscardTriggers(GameData gameData, UUID discardingPlayerId, Card discardedCard) {
        checkDiscardTriggers(gameData, discardingPlayerId, discardedCard, false);
    }

    /**
     * Whether a discarded card sits in a library instead of a graveyard, i.e. it was put there by a
     * discard replacement (Library of Leng) without being revealed. The library-top replacement never
     * reveals the card, so its characteristics are undefined for discard triggers.
     */
    private boolean isDiscardedToLibraryUnrevealed(GameData gameData, Card discardedCard) {
        boolean inGraveyard = gameData.playerGraveyards.values().stream()
                .anyMatch(graveyard -> graveyard.stream().anyMatch(card -> card.getId().equals(discardedCard.getId())));
        if (inGraveyard) {
            return false;
        }
        return gameData.playerDecks.values().stream()
                .anyMatch(deck -> deck.stream().anyMatch(card -> card.getId().equals(discardedCard.getId())));
    }

    public void checkDiscardTriggers(GameData gameData, UUID discardingPlayerId, Card discardedCard,
                                     boolean cycled) {
        if (cycled) {
            gameData.recordCardCycled(discardingPlayerId, discardedCard);
        }
        // Central discard hook: every discard path routes through here, so count discards per player
        // for this turn (Dream Salvage's "cards target opponent discarded this turn").
        gameData.cardsDiscardedThisTurn.merge(discardingPlayerId, 1, Integer::sum);
        if (discardedCard != null && !discardedCard.hasType(CardType.LAND)) {
            gameData.nonlandCardsDiscardedThisTurn.merge(discardingPlayerId, 1, Integer::sum);
        }
        // Also remember which specific cards were discarded (cycling is a discard) so a later effect can
        // return them from the graveyard (Shadow of the Grave).
        // Remember the mana value of the card just discarded, so a later effect of the same spell can
        // scale off it (Blast of Genius's "damage equal to the discarded card's mana value").
        if (discardedCard != null) {
            gameData.lastDiscardedCardManaValue = discardedCard.getManaValue();
            gameData.greatestDiscardedCardManaValue = Math.max(
                    gameData.greatestDiscardedCardManaValue, discardedCard.getManaValue());
            EnumSet<CardType> cardTypes = EnumSet.noneOf(CardType.class);
            if (discardedCard.getType() != null) {
                cardTypes.add(discardedCard.getType());
            }
            cardTypes.addAll(discardedCard.getAdditionalTypes());
            gameData.lastDiscardedCardTypes = Set.copyOf(cardTypes);
        } else {
            gameData.lastDiscardedCardTypes = Set.of();
        }
        if (discardedCard != null && !discardedCard.isToken()) {
            gameData.cardsDiscardedOrCycledThisTurn
                    .computeIfAbsent(discardingPlayerId, ignored -> ConcurrentHashMap.newKeySet())
                    .add(discardedCard.getId());
            if (gameData.discardCausedByOpponent) {
                gameData.cardsDiscardedByOpponentThisTurn
                        .computeIfAbsent(discardingPlayerId, ignored -> ConcurrentHashMap.newKeySet())
                        .add(discardedCard.getId());
            }
        }

        if (discardedCard != null && gameData.discardEventPlayerId != null) {
            gameData.discardEventCardCount++;
            gameData.discardEventCards.add(discardedCard);
        }

        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.Discard(discardingPlayerId, discardedCard, cycled);
        // A card put into a hidden zone unrevealed (Library of Leng) has undefined characteristics
        // (CR 701.9c), so no type-gated discard trigger can match it.
        boolean characteristicsKnown = discardedCard != null
                && !isDiscardedToLibraryUnrevealed(gameData, discardedCard);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(discardingPlayerId)) return;

            for (Permanent perm : battlefield) {
                if (gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
                for (CardEffect effect : LibraryWatch.ON_OPPONENT_DISCARDS.effectsOf(perm.getCard())) {
                    // "Whenever an opponent discards a creature/land/noncreature-nonland card, …"
                    // (Waste Not) — the discarded card gates the trigger.
                    CardEffect resolved = !characteristicsKnown
                            ? (effect instanceof TriggeringCardConditionalEffect ? null : effect)
                            : unwrapTriggeringCardConditional(effect, discardedCard, gameData, playerId);
                    if (resolved == null) continue;
                    if (dispatchSlotEffect(gameData, perm, playerId,
                            LibraryWatch.ON_OPPONENT_DISCARDS, ctx, resolved)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        if (collectTemporaryGlobalDiscardTriggers(gameData, discardingPlayerId, discardedCard)) {
            anyTriggered[0] = true;
        }

        // "Whenever you discard a card" — scan the discarding player's own battlefield (e.g. Necropotence).
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(discardingPlayerId);
        if (ownBattlefield != null) {
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                for (CardEffect effect : LibraryWatch.ON_CONTROLLER_DISCARDS.effectsOf(perm.getCard())) {
                    CardEffect resolved = !characteristicsKnown
                            ? (effect instanceof TriggeringCardConditionalEffect ? null : effect)
                            : unwrapTriggeringCardConditional(effect, discardedCard, gameData, discardingPlayerId);
                    if (resolved == null) continue;
                    boolean firstCycling = resolved instanceof OncePerTurnTriggerEffect once && once.firstCycling();
                    boolean firstNonlandDiscard = resolved instanceof OncePerTurnTriggerEffect once
                            && OncePerTurnTriggerEffect.FIRST_NONLAND_DISCARD_KEY.equals(once.key());
                    boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect
                            && !firstCycling && !firstNonlandDiscard;
                    if (firstNonlandDiscard) {
                        if (gameData.nonlandCardsDiscardedThisTurn.getOrDefault(discardingPlayerId, 0) != 1) continue;
                        resolved = ((OncePerTurnTriggerEffect) resolved).wrapped();
                    } else if (firstCycling) {
                        if (!cycled || gameData.cardsCycledThisTurn.getOrDefault(discardingPlayerId, 0) != 1) continue;
                        resolved = ((OncePerTurnTriggerEffect) resolved).wrapped();
                    } else {
                        resolved = unwrapOncePerTurnTrigger(gameData, perm, resolved);
                        if (resolved == null) continue;
                    }
                    var match = new TriggerMatchContext(gameData, perm, discardingPlayerId, resolved);
                    boolean triggered = dispatch(match, LibraryWatch.ON_CONTROLLER_DISCARDS, resolved, ctx);
                    if (triggered && oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                    }
                    if (triggered) {
                        anyTriggered[0] = true;
                    }
                }
            }
        }

        if (discardedCard != null && gameData.discardEventPlayerId == null
                && checkDiscardEventTriggers(gameData, discardingPlayerId, 1, List.of(discardedCard))) {
            anyTriggered[0] = true;
        }

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }

        // Process any pending may abilities added by discard triggers
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }

        // Check the discarded card itself for self-discard triggers
        if (discardedCard != null) {
            // "When you discard this card" — any discard (Edgar's Awakening). Non-targeting effects
            // (e.g. MayPayManaEffect) go straight onto the stack; any-target effects use the
            // DiscardTriggerAnyTarget pipeline (same as Guerrilla Tactics).
            List<CardEffect> anyDiscardTriggers = LibraryWatch.ON_SELF_DISCARDED.effectsOf(discardedCard);
            if (!anyDiscardTriggers.isEmpty()) {
                boolean needsAnyTarget = anyDiscardTriggers.stream()
                        .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                if (needsAnyTarget) {
                    gameData.queueInteraction(new PermanentChoiceContext.DiscardTriggerAnyTarget(
                            discardedCard, discardingPlayerId, new ArrayList<>(anyDiscardTriggers)
                    ));
                } else {
                    gameData.enqueueTrigger(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            discardedCard,
                            discardingPlayerId,
                            discardedCard.getName() + "'s ability",
                            new ArrayList<>(anyDiscardTriggers)
                    ));
                }
                gameLogService.append(gameData, GameLog.cardThen(discardedCard,
                        " was discarded — its ability triggers!"));
                log.info("Game {} - {} ON_SELF_DISCARDED trigger queued", gameData.id, discardedCard.getName());
                anyTriggered[0] = true;
            }

            // Skip EnterBattlefieldOnDiscardEffect — it's a replacement effect handled earlier in the discard flow
            if (gameData.discardCausedByOpponent) {
                List<CardEffect> selfTriggers = LibraryWatch.ON_SELF_DISCARDED_BY_OPPONENT.effectsOf(discardedCard).stream()
                        .filter(e -> !(e instanceof EnterBattlefieldOnDiscardEffect))
                        .filter(e -> !(e instanceof ConditionalEffect conditional) || !conditional.interveningIf()
                                || conditionEvaluationService.isMet(gameData, conditional.condition(),
                                        ConditionContext.forCard(discardedCard, discardingPlayerId)))
                        .toList();
                if (!selfTriggers.isEmpty()) {
                    boolean needsAnyTarget = selfTriggers.stream()
                            .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                                    || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
                    if (needsAnyTarget) {
                        gameData.queueInteraction(new PermanentChoiceContext.DiscardTriggerAnyTarget(
                                discardedCard, discardingPlayerId, new ArrayList<>(selfTriggers)
                        ));
                    } else {
                        StackEntry selfDiscardEntry = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                discardedCard,
                                discardingPlayerId,
                                discardedCard.getName() + "'s ability",
                                new ArrayList<>(selfTriggers)
                        );
                        selfDiscardEntry.setTriggeringCardGraveyardEntryVersion(
                                gameData.graveyardEntryVersion(discardedCard.getId()));
                        gameData.enqueueTrigger(selfDiscardEntry);
                    }
                    gameLogService.append(gameData, GameLog.cardThen(discardedCard, " was discarded by an opponent's effect — its ability triggers!"));
                    log.info("Game {} - {} self-discard trigger queued", gameData.id, discardedCard.getName());
                }
            }
        }
    }

    // ── Source deals damage to a player (noncombat) ────────────────────

    public void checkCycleTriggers(GameData gameData, UUID cyclingPlayerId, Card cycledCard) {
        checkCycleTriggers(gameData, cyclingPlayerId, cycledCard, 0);
    }

    /**
     * As {@link #checkCycleTriggers(GameData, UUID, Card)}, carrying the X paid for the cycling cost so
     * that the card's own "when you cycle" trigger can read it (Shark Typhoon).
     */
    public void checkCycleTriggers(GameData gameData, UUID cyclingPlayerId, Card cycledCard, int cyclingXValue) {
        checkCycleTriggers(gameData, cyclingPlayerId, cycledCard, cyclingXValue, null, null);
    }

    /**
     * As {@link #checkCycleTriggers(GameData, UUID, Card, int)}, for a cycling ability whose announced
     * targets belong to the card's own "when you cycle this card" trigger (Rampaging War Mammoth):
     * that trigger goes on the stack above the cycling ability with those targets.
     *
     * @param targetingCyclingAbility the cycling ability that announced the targets, or {@code null}
     * @param announcedTargetIds      the targets announced with the cycling ability
     */
    public void checkCycleTriggers(GameData gameData, UUID cyclingPlayerId, Card cycledCard, int cyclingXValue,
                                   com.github.laxika.magicalvibes.model.ActivatedAbility targetingCyclingAbility,
                                   List<UUID> announcedTargetIds) {
        List<CardEffect> selfEffects = LibraryWatch.ON_SELF_CYCLED.effectsOf(cycledCard).stream()
                .filter(e -> !(e instanceof ConditionalEffect conditional) || !conditional.interveningIf()
                        || conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forCard(cycledCard, cyclingPlayerId)))
                .toList();
        if (!selfEffects.isEmpty()) {
            List<UUID> announcedTargets = announcedTargetIds != null ? announcedTargetIds : List.of();
            boolean chooseOnTrigger = targetingCyclingAbility != null && announcedTargets.isEmpty()
                    && targetingCyclingAbility.isCyclingTriggerTargetsChosenOnTriggerIfOmitted();
            if (targetingCyclingAbility != null && !chooseOnTrigger) {
                List<UUID> targetIds = announcedTargets;
                if (!targetIds.isEmpty()) {
                    StackEntry selfCycleEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, cycledCard,
                            cyclingPlayerId, cycledCard.getName() + "'s ability", new ArrayList<>(selfEffects),
                            cyclingXValue, null, null, Map.of(), null, List.of(), targetIds);
                    selfCycleEntry.setTargetFilter(targetingCyclingAbility.getTargetFilter());
                    if (targetIds.size() == 1) {
                        selfCycleEntry.setTargetId(targetIds.getFirst());
                    }
                    selfCycleEntry.setMultiTargetConstraint(targetingCyclingAbility.getMultiTargetConstraint());
                    gameData.enqueueTrigger(selfCycleEntry);
                } else if (targetingCyclingAbility.isCyclingTriggerWithoutTargets()) {
                    StackEntry selfCycleEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, cycledCard,
                            cyclingPlayerId, cycledCard.getName() + "'s ability", new ArrayList<>(selfEffects));
                    selfCycleEntry.setXValue(cyclingXValue);
                    selfCycleEntry.setNonTargeting(true);
                    gameData.enqueueTrigger(selfCycleEntry);
                }
            } else if (selfEffects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.SPELL))) {
                com.github.laxika.magicalvibes.model.filter.StackEntryPredicate cycleSpellFilter = null;
                boolean cycleIncludesAbilities = false;
                if (cycledCard.getTargetFilter() instanceof com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter cycleFilter) {
                    cycleSpellFilter = cycleFilter.predicate();
                    cycleIncludesAbilities = predicateContainsHasTarget(cycleFilter.predicate());
                }
                gameData.queueInteraction(new PermanentChoiceContext.ETBSpellTargetTrigger(
                        cycledCard, cyclingPlayerId, new ArrayList<>(selfEffects), cycleSpellFilter,
                        cycleIncludesAbilities, null));
            } else if (selfEffects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
                gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                        cycledCard, cyclingPlayerId, new ArrayList<>(selfEffects), null));
            } else if (graveyardTargetingSupport.findTarget(selfEffects) != null) {
                // CR 603.3d: a "when you cycle this card" trigger targeting a card in a graveyard
                // chooses it as the trigger goes on the stack (Grixis Sojourners).
                gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        cycledCard, cyclingPlayerId, new ArrayList<>(selfEffects)));
            } else {
                StackEntry selfCycleEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, cycledCard,
                        cyclingPlayerId, cycledCard.getName() + "'s ability", new ArrayList<>(selfEffects));
                selfCycleEntry.setXValue(cyclingXValue);
                gameData.enqueueTrigger(selfCycleEntry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(cycledCard));
        }
        TriggerContext ctx = new TriggerContext.Cycle(cyclingPlayerId, cycledCard);
        boolean[] anyTriggered = {false};
        gameData.forEachPermanent((playerId, perm) -> {
            if (dispatchWatch(gameData, perm, playerId, LibraryWatch.ON_ANY_PLAYER_CYCLES, ctx)) {
                anyTriggered[0] = true;
            }
        });
        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    private boolean collectTemporaryGlobalDiscardTriggers(GameData gameData, UUID discardingPlayerId,
                                                          Card discardedCard) {
        if (!gameData.discardCausedByOpponent) {
            return false;
        }

        boolean triggered = false;
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(LibraryWatch.ON_OPPONENT_DISCARDS.trigger())
                    || !watcher.controllerId().equals(discardingPlayerId)) {
                continue;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setTargetId(discardingPlayerId);
            entry.setNonTargeting(true);
            if (discardedCard != null) {
                entry.setEventCardIds(List.of(discardedCard.getId()));
            }
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary global discard trigger fires", gameData.id,
                    watcher.sourceCard().getName());
            triggered = true;
        }
        return triggered;
    }

    /**
     * Queues {@link DamageWatch#ON_DAMAGE_TO_PLAYER} triggers when a permanent deals noncombat
     * damage to a player (e.g. Niv-Mizzet, Dracogenius's ping). Combat damage uses the richer
     * collector in {@code CombatDamageService} instead — do not call this from the combat path.
     */
    public void checkScryTriggers(GameData gameData, UUID scryingPlayerId) {
        checkScryTriggers(gameData, scryingPlayerId, 0, 0);
    }

    public void checkScryTriggers(GameData gameData, UUID scryingPlayerId, int bottomedCardCount) {
        checkScryTriggers(gameData, scryingPlayerId, bottomedCardCount, 0);
    }

    public void checkScryTriggers(GameData gameData, UUID scryingPlayerId,
                                  int bottomedCardCount, int cardsLookedAt) {
        var ctx = new TriggerContext.Scry(scryingPlayerId, bottomedCardCount, cardsLookedAt);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(scryingPlayerId);
        if (ownBattlefield != null) {
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                dispatchWatch(gameData, perm, scryingPlayerId, LibraryWatch.ON_CONTROLLER_SCRIES, ctx);
            }
        }

        gameData.forEachBattlefield((controllerId, battlefield) -> {
            if (controllerId.equals(scryingPlayerId)) {
                return;
            }
            for (Permanent perm : List.copyOf(battlefield)) {
                dispatchWatch(gameData, perm, controllerId, LibraryWatch.ON_OPPONENT_SCRIES, ctx);
            }
        });
    }

    /** Fires triggers that care about all players finishing a voting event. */
    public void checkVotingFinishedTriggers(GameData gameData,
                                             com.github.laxika.magicalvibes.model.VotingResult result) {
        TriggerContext context = new TriggerContext.VotingFinished(result);
        gameData.forEachPermanent((controllerId, permanent) ->
                dispatchWatch(gameData, permanent, controllerId,
                        ActionWatch.ON_PLAYERS_FINISH_VOTING, context));
    }

    /** Fires triggers whenever a player is tempted by the Ring. */
    public void checkRingTemptTriggers(GameData gameData, UUID temptingPlayerId, UUID ringBearerId) {
        if (temptingPlayerId == null || ringBearerId == null) {
            return;
        }
        var context = new TriggerContext.RingTempted(temptingPlayerId, ringBearerId);
        for (Permanent permanent : List.copyOf(
                gameData.playerBattlefields.getOrDefault(temptingPlayerId, List.of()))) {
            dispatchWatch(gameData, permanent, temptingPlayerId,
                    ActionWatch.ON_CONTROLLER_TEMPTS_RING, context);
        }
    }

    public void checkRingTemptsYouTriggers(GameData gameData, UUID temptedPlayerId) {
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(temptedPlayerId);
        if (ownBattlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.RingTemptsYou(temptedPlayerId);
        for (Permanent permanent : List.copyOf(ownBattlefield)) {
            dispatchWatch(gameData, permanent, temptedPlayerId, ActionWatch.ON_RING_TEMPTS_YOU, context);
        }
    }

    public void checkInvestigateTriggers(GameData gameData, UUID investigatingPlayerId) {
        boolean firstInvestigationThisTurn = gameData.playersWhoInvestigatedThisTurn.add(investigatingPlayerId);
        var ctx = new TriggerContext.Investigate(investigatingPlayerId);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(investigatingPlayerId);
        if (ownBattlefield == null) {
            return;
        }

        for (Permanent perm : List.copyOf(ownBattlefield)) {
            if (firstInvestigationThisTurn) {
                dispatchWatch(gameData, perm, investigatingPlayerId, ActionWatch.ON_CONTROLLER_INVESTIGATES, ctx);
            }
            dispatchWatch(gameData, perm, investigatingPlayerId,
                    ActionWatch.ON_CONTROLLER_INVESTIGATES_EACH_TIME, ctx);
        }
    }

    /** Fires triggers whenever a player seeks one or more cards. */
    public void checkSeekTriggers(GameData gameData, UUID seekingPlayerId) {
        checkSeekTriggers(gameData, seekingPlayerId, List.of());
    }

    /** Fires triggers whenever the given player rolls one or more dice. */
    public void checkControllerRollsPlanarDieTriggers(GameData gameData, UUID rollingPlayerId) {
        checkControllerRollsPlanarDieTriggers(gameData, rollingPlayerId, 0);
    }

    /** Fires triggers whenever the given player rolls the planar die, preserving blank/nonblank. */
    public void checkControllerRollsPlanarDieTriggers(GameData gameData, UUID rollingPlayerId,
                                                       int result) {
        for (Permanent perm : List.copyOf(gameData.playerBattlefields.getOrDefault(rollingPlayerId, List.of()))) {
            dispatchWatch(gameData, perm, rollingPlayerId, ActionWatch.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE,
                    new TriggerContext.DiceRoll(rollingPlayerId, 1, result, true));
        }
        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || !gameData.planechase.controllerId.equals(rollingPlayerId)) {
            return;
        }
        UUID planarControllerId = gameData.planechase.controllerId;
        TriggerContext context = new TriggerContext.DiceRoll(rollingPlayerId, 1, result, true);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (CardEffect effect : ActionWatch.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE.effectsOf(object.getCard())) {
                registry.dispatch(new TriggerMatchContext(gameData, null, planarControllerId, effect,
                                object.getCard(), object.copy()),
                        ActionWatch.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE, effect, context);
            }
        }
    }

    public void checkControllerRollsOneOrMoreDiceTriggers(GameData gameData, UUID rollingPlayerId,
                                                           int diceCount) {
        checkControllerRollsOneOrMoreDiceTriggers(gameData, rollingPlayerId, diceCount, 0);
    }

    /** Fires triggers whenever the given player rolls one or more dice, preserving the result. */
    public void checkControllerRollsOneOrMoreDiceTriggers(GameData gameData, UUID rollingPlayerId,
                                                           int diceCount, int result) {
        if (rollingPlayerId == null || diceCount <= 0) return;

        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(rollingPlayerId);
        if (ownBattlefield == null) return;

        var ctx = new TriggerContext.DiceRoll(rollingPlayerId, diceCount, result);
        for (Permanent perm : List.copyOf(ownBattlefield)) {
            dispatchWatch(gameData, perm, rollingPlayerId,
                    ActionWatch.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE, ctx);
        }
    }

    /** Fires one trigger for each die whose natural result was that die's highest face. */
    public void checkControllerRollsHighestNaturalResultTriggers(GameData gameData,
                                                                  UUID rollingPlayerId,
                                                                  int sides,
                                                                  int... results) {
        if (rollingPlayerId == null || sides < 1 || results == null || results.length == 0) return;

        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(rollingPlayerId);
        if (ownBattlefield == null) return;

        for (int result : results) {
            var dieContext = new TriggerContext.DiceRoll(rollingPlayerId, 1, result);
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                dispatchWatch(gameData, perm, rollingPlayerId, ActionWatch.ON_CONTROLLER_ROLLS_DIE, dieContext);
            }
            if (result != sides) continue;
            var ctx = new TriggerContext.DiceRoll(rollingPlayerId, 1, result);
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                dispatchWatch(gameData, perm, rollingPlayerId,
                        ActionWatch.ON_CONTROLLER_ROLLS_HIGHEST_NATURAL_RESULT, ctx);
            }
        }
    }

    /** Fires graveyard-resident triggers whenever the given player rolls a natural 20. */
    public void checkControllerRollsNaturalTwentyTriggers(GameData gameData, UUID rollingPlayerId) {
        if (rollingPlayerId == null) return;

        List<Card> graveyard = gameData.playerGraveyards.get(rollingPlayerId);
        if (graveyard == null) return;

        for (Card card : List.copyOf(graveyard)) {
            for (CardEffect effect : ActionWatch.GRAVEYARD_ON_CONTROLLER_ROLLS_NATURAL_20.effectsOf(card)) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        rollingPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} graveyard ability triggers on a natural 20",
                        gameData.id, card.getName());
            }
        }
    }

    public void checkForageTriggers(GameData gameData, UUID foragingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(foragingPlayerId);
        if (battlefield == null) {
            return;
        }
        var context = new TriggerContext.Forage(foragingPlayerId);
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchWatch(gameData, permanent, foragingPlayerId, ActionWatch.ON_CONTROLLER_FORAGES, context);
        }
    }

    public void checkSeekTriggers(GameData gameData, UUID seekingPlayerId, List<Card> soughtCards) {
        if (seekingPlayerId == null) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(seekingPlayerId);
        if (battlefield == null) {
            return;
        }
        TriggerContext context = new TriggerContext.Seek(
                seekingPlayerId, soughtCards == null ? List.of() : soughtCards);
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchWatch(gameData, permanent, seekingPlayerId, ActionWatch.ON_CONTROLLER_SEEKS, context);
        }
    }

    public void checkSurveilTriggers(GameData gameData, UUID surveilingPlayerId) {
        boolean firstThisTurn = gameData.playersWhoSurveilledThisTurn.add(surveilingPlayerId);
        var ctx = new TriggerContext.Surveil(surveilingPlayerId, firstThisTurn);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(surveilingPlayerId);
        if (ownBattlefield != null) {
            for (Permanent perm : List.copyOf(ownBattlefield)) {
                dispatchWatch(gameData, perm, surveilingPlayerId, LibraryWatch.ON_CONTROLLER_SURVEILS, ctx);
            }
        }

        gameData.forEachBattlefield((controllerId, battlefield) -> {
            if (controllerId.equals(surveilingPlayerId)) {
                return;
            }
            for (Permanent perm : List.copyOf(battlefield)) {
                dispatchWatch(gameData, perm, controllerId, LibraryWatch.ON_OPPONENT_SURVEILS, ctx);
            }
        });

        List<Card> graveyard = gameData.playerGraveyards.get(surveilingPlayerId);
        if (graveyard == null) {
            return;
        }

        for (Card card : List.copyOf(graveyard)) {
            for (CardEffect effect : gameQueryService.getEffectiveGraveyardTriggerEffects(
                    gameData, card, LibraryWatch.GRAVEYARD_ON_CONTROLLER_SURVEILS.trigger())) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        surveilingPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} graveyard ability triggers on surveil",
                        gameData.id, card.getName());
            }
        }
    }

    public void checkManifestDreadTriggers(GameData gameData, UUID manifestingPlayerId,
                                           Card cardPutIntoGraveyard) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(manifestingPlayerId);
        if (battlefield == null) {
            return;
        }

        var context = new TriggerContext.ManifestDread(manifestingPlayerId, cardPutIntoGraveyard);
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchWatch(gameData, permanent, manifestingPlayerId,
                    ActionWatch.ON_CONTROLLER_MANIFESTS_DREAD, context);
        }
    }

    /** Fires battlefield and graveyard-resident triggers whenever a player completes a dungeon. */
    /** Completes a bottom room after its last room ability leaves the stack. */
    public void completeDungeonRoomIfReady(GameData gameData, StackEntry departedEntry) {
        var completion = departedEntry.getEffectsToResolve().stream()
                .filter(com.github.laxika.magicalvibes.model.effect.CompleteDungeonEffect.class::isInstance)
                .map(com.github.laxika.magicalvibes.model.effect.CompleteDungeonEffect.class::cast)
                .findFirst().orElse(null);
        if (completion == null) return;
        var progress = gameData.playerDungeonProgress.get(departedEntry.getControllerId());
        if (progress == null || progress.dungeon() != completion.dungeon() || !progress.isBottomRoom()) return;
        boolean anotherRoomAbility = gameData.stack.stream().anyMatch(entry ->
                entry.getControllerId().equals(departedEntry.getControllerId())
                        && entry.getEffectsToResolve().stream().anyMatch(effect ->
                        effect instanceof com.github.laxika.magicalvibes.model.effect.CompleteDungeonEffect other
                                && other.dungeon() == completion.dungeon()));
        if (anotherRoomAbility) return;
        gameData.playerDungeonProgress.remove(departedEntry.getControllerId());
        gameData.recordCompletedDungeon(departedEntry.getControllerId(), completion.dungeon());
        checkDungeonCompletionTriggers(gameData, departedEntry.getControllerId());
    }

    public void checkDungeonCompletionTriggers(GameData gameData, UUID completingPlayerId) {
        if (completingPlayerId == null) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(completingPlayerId);
        if (battlefield != null) {
            TriggerContext context = new TriggerContext.DungeonCompletion(completingPlayerId);
            for (Permanent permanent : List.copyOf(battlefield)) {
                dispatchWatch(gameData, permanent, completingPlayerId,
                        ActionWatch.ON_CONTROLLER_COMPLETES_DUNGEON, context);
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(completingPlayerId);
        if (graveyard == null) {
            return;
        }

        for (Card card : List.copyOf(graveyard)) {
            for (CardEffect effect : ActionWatch.GRAVEYARD_ON_CONTROLLER_COMPLETES_DUNGEON.effectsOf(card)) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        completingPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                );
                entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} graveyard ability triggers on dungeon completion",
                        gameData.id, card.getName());
            }
        }
    }

    public void checkDiscoverTriggers(GameData gameData, UUID discoveringPlayerId, int discoverValue) {
        var ctx = new TriggerContext.Discover(discoveringPlayerId, discoverValue);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(discoveringPlayerId);
        if (ownBattlefield == null) {
            return;
        }
        for (Permanent perm : List.copyOf(ownBattlefield)) {
            dispatchWatch(gameData, perm, discoveringPlayerId, ActionWatch.ON_CONTROLLER_DISCOVERS, ctx);
        }
    }

    public void checkConjureTriggers(GameData gameData, UUID conjuringPlayerId, int cardCount) {
        if (conjuringPlayerId == null || cardCount <= 0) {
            return;
        }
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(conjuringPlayerId);
        if (ownBattlefield != null) {
            var context = new TriggerContext.Conjure(conjuringPlayerId, cardCount);
            for (Permanent permanent : List.copyOf(ownBattlefield)) {
                dispatchWatch(gameData, permanent, conjuringPlayerId,
                        ActionWatch.ON_CONTROLLER_CONJURES, context);
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(conjuringPlayerId);
        if (graveyard == null) {
            return;
        }
        for (Card card : List.copyOf(graveyard)) {
            for (CardEffect effect : gameQueryService.getEffectiveGraveyardTriggerEffects(
                    gameData, card, ActionWatch.GRAVEYARD_ON_CONTROLLER_CONJURES.trigger())) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        conjuringPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} graveyard ability triggers on conjure",
                        gameData.id, card.getName());
            }
        }
    }

    /**
     * Fires turn-scoped global watchers registered for {@code ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER}
     * (e.g. The Sea Devils chapter III). The watcher's trigger source is the dealing creature, so
     * effects that "it deals damage" use that creature, and the damaged player is bound as the
     * trigger's target. A {@link TriggeringPermanentConditionalEffect} wrapper filters the dealer.
     */
    private void enqueueTemporaryAnyCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature,
                                                                         UUID creatureControllerId,
                                                                         UUID damagedPlayerId, int damageDealt) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER.trigger())
                    || watcher.watchedPermanentId() != null) {
                continue;
            }
            CardEffect resolvedEffect = watcher.effect();
            if (resolvedEffect instanceof TriggeringPermanentConditionalEffect conditional) {
                if (conditional.predicate() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(creature, conditional.predicate(),
                        FilterContext.of(gameData)
                                .withSourceCardId(watcher.sourceCard().getId())
                                .withSourceControllerId(watcher.controllerId()))) {
                    continue;
                }
                resolvedEffect = conditional.wrapped();
            }
            if (resolvedEffect instanceof DamagedPlayerControlsTargetEffect damagedPlayerTarget) {
                // The target is chosen as the trigger is put on the stack (CR 603.3d), by the
                // watcher's controller, from the creatures the damaged player controls.
                gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                        watcher.sourceCard(), watcher.controllerId(),
                        List.of(damagedPlayerTarget.forDamagedPlayer(damagedPlayerId, damageDealt)),
                        creature.getId(),
                        damagedPlayerTarget.targetChooserId(damagedPlayerId, watcher.controllerId()),
                        damagedPlayerId, creature.getId()));
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s triggered ability",
                    new ArrayList<>(List.of(resolvedEffect)),
                    damagedPlayerId,
                    creature.getId());
            entry.setTriggeringPermanentId(creature.getId());
            entry.setTriggeringPermanentControllerId(creatureControllerId);
            entry.setEventValue(damageDealt);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary global ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    public void checkAnyCreatureCombatDamageToOpponentTriggers(GameData gameData, Permanent creature,
                                                               UUID creatureControllerId,
                                                               UUID damagedPlayerId, int damageDealt) {
        if (creature == null || creatureControllerId == null || damagedPlayerId == null || damageDealt <= 0) {
            return;
        }

        enqueueTemporaryAnyCreatureCombatDamageToPlayerTriggers(gameData, creature, creatureControllerId,
                damagedPlayerId, damageDealt);

        gameData.forEachPermanent((controllerId, source) -> {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) {
                return;
            }

            boolean printedAbilitiesLost = gameQueryService.hasLostPrintedAbilities(gameData, source);
            List<CardEffect> effects = new ArrayList<>();
            if (!controllerId.equals(damagedPlayerId)) {
                if (!printedAbilitiesLost) {
                    effects.addAll(CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT.effectsOf(source.getCard()));
                }
                effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                        gameData, source, CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT.trigger()));
            }
            if (!printedAbilitiesLost) {
                effects.addAll(CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER.effectsOf(source.getCard()));
            }
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source, CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER.trigger()));
            for (CardEffect effect : effects) {
                CardEffect resolvedEffect = effect;
                boolean matches = true;
                while (resolvedEffect instanceof TriggeringPermanentControllerConditionalEffect conditional) {
                    if (!controllerId.equals(creatureControllerId)) {
                        matches = false;
                        break;
                    }
                    resolvedEffect = conditional.wrapped();
                }
                while (matches && resolvedEffect instanceof TriggeringPermanentConditionalEffect conditional) {
                    if (!predicateEvaluationService.matchesPermanentPredicate(creature,
                            conditional.predicate(),
                            FilterContext.of(gameData).withSourceControllerId(controllerId))) {
                        matches = false;
                        break;
                    }
                    resolvedEffect = conditional.wrapped();
                }
                if (!matches) {
                    continue;
                }
                if (resolvedEffect instanceof ConditionalEffect conditional && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(source, controllerId).withTargetId(damagedPlayerId))) {
                    continue;
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        controllerId,
                        source.getCard().getName() + "'s triggered ability",
                        List.of(resolvedEffect),
                        damagedPlayerId,
                        source.getId());
                entry.setTriggeringPermanentId(creature.getId());
                entry.setTriggeringPermanentControllerId(creatureControllerId);
                entry.setNonTargeting(true);
                int previousCopies = beginCombatDamageTriggerCopies(
                        gameData, controllerId, source, creature, creatureControllerId, damageDealt);
                try {
                    gameData.enqueueTrigger(entry);
                    gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
                }
                log.info("Game {} - {} triggers when a creature deals combat damage to an opponent",
                        gameData.id, source.getCard().getName());
            }
        });

        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || gameData.planechase.controllerId.equals(damagedPlayerId)) {
            return;
        }
        UUID planarControllerId = gameData.planechase.controllerId;
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (CardEffect effect : CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT.effectsOf(object.getCard())) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        object.getCard(),
                        planarControllerId,
                        object.getCard().getName() + "'s triggered ability",
                        List.of(effect),
                        creatureControllerId,
                        (UUID) null);
                entry.setTriggeringPermanentId(creature.getId());
                entry.setTriggeringPermanentControllerId(creatureControllerId);
                entry.setSourcePlanarObject(object.copy());
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(object.getCard()));
                log.info("Game {} - {} triggers when a creature deals combat damage to an opponent",
                        gameData.id, object.getCard().getName());
            }
        }
    }

    /**
     * Queues the "when you're dealt combat damage" triggers of the damaged player's permanents,
     * once for the combat damage dealt to that player in a damage step.
     */
    public void checkControllerDealtCombatDamageTriggers(GameData gameData, UUID damagedPlayerId, int damageDealt) {
        if (damagedPlayerId == null || damageDealt <= 0) {
            return;
        }
        for (Permanent source : new ArrayList<>(gameData.playerBattlefields.getOrDefault(damagedPlayerId, List.of()))) {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()
                    || gameQueryService.hasLostPrintedAbilities(gameData, source)) {
                continue;
            }
            for (CardEffect effect : CombatWatch.ON_CONTROLLER_DEALT_COMBAT_DAMAGE.effectsOf(source.getCard())) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        damagedPlayerId,
                        source.getCard().getName() + "'s triggered ability",
                        List.of(effect),
                        null,
                        source.getId());
                entry.setNonTargeting(true);
                entry.setEventValue(damageDealt);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
                log.info("Game {} - {} triggers when its controller is dealt combat damage",
                        gameData.id, source.getCard().getName());
            }
        }
    }

    public void checkAnyCreatureCombatDamageToOwnerTriggers(GameData gameData, Permanent creature,
                                                            UUID creatureControllerId,
                                                            UUID damagedPlayerId, int damageDealt) {
        if (creature == null || creatureControllerId == null || damagedPlayerId == null || damageDealt <= 0
                || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }
        UUID creatureOwnerId = gameData.playerCommanders.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(card -> card.getId().equals(creature.getOriginalCard().getId())))
                .map(Map.Entry::getKey).findFirst().orElse(creature.getCard().getOwnerId());
        if (!damagedPlayerId.equals(creatureOwnerId)) return;

        gameData.forEachPermanent((controllerId, source) -> {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) {
                return;
            }

            List<CardEffect> effects = new ArrayList<>(
                    gameQueryService.hasLostPrintedAbilities(gameData, source) ? List.of()
                            : CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OWNER.effectsOf(source.getCard()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source, CombatWatch.ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OWNER.trigger()));
            for (CardEffect effect : effects) {
                CardEffect resolvedEffect = effect;
                boolean matches = true;
                while (resolvedEffect instanceof TriggeringPermanentControllerConditionalEffect conditional) {
                    if (!controllerId.equals(creatureControllerId)) {
                        matches = false;
                        break;
                    }
                    resolvedEffect = conditional.wrapped();
                }
                while (matches && resolvedEffect instanceof TriggeringPermanentConditionalEffect conditional) {
                    if (!predicateEvaluationService.matchesPermanentPredicate(creature,
                            conditional.predicate(),
                            FilterContext.of(gameData).withSourceControllerId(controllerId))) {
                        matches = false;
                        break;
                    }
                    resolvedEffect = conditional.wrapped();
                }
                if (!matches) {
                    continue;
                }
                if (resolvedEffect instanceof ConditionalEffect conditional && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(source, controllerId))) {
                    continue;
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        controllerId,
                        source.getCard().getName() + "'s triggered ability",
                        List.of(resolvedEffect),
                        null,
                        source.getId());
                entry.setTriggeringPermanentId(creature.getId());
                entry.setTriggeringPermanentControllerId(creatureControllerId);
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
                log.info("Game {} - {} triggers when a creature deals combat damage to its owner",
                        gameData.id, source.getCard().getName());
            }
        });
    }

    private int beginCombatDamageTriggerCopies(GameData gameData, UUID abilityControllerId,
                                                Permanent abilitySource, Permanent damageSource,
                                                UUID damageSourceControllerId, int damageToPlayers) {
        if (abilityControllerId == null || abilitySource == null || damageSource == null) {
            return gameData.beginTriggeredAbilityCopies(1);
        }
        TriggerContext.SourceDealsCombatDamage context = new TriggerContext.SourceDealsCombatDamage(
                damageSource.getCard(), damageSourceControllerId, damageSource.getId(),
                damageToPlayers, damageToPlayers);
        return gameData.beginTriggeredAbilityCopies(1
                + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                gameData, abilityControllerId, abilitySource, false, context));
    }

    /**
     * Fires the batched trigger for one or more goaded creatures dealing combat damage to an
     * opponent. The damage dealers are supplied as one combat-damage batch so multiple goaded
     * creatures cause only one trigger.
     */
    public void checkGoadedCreatureCombatDamageToOpponentTriggers(GameData gameData,
                                                                   UUID damagedPlayerId,
                                                                   List<Permanent> damageDealers) {
        if (damagedPlayerId == null || damageDealers == null || damageDealers.isEmpty()) {
            return;
        }
        boolean goadedCreatureDealtDamage = damageDealers.stream()
                .anyMatch(creature -> gameQueryService.isGoaded(gameData, creature));
        if (!goadedCreatureDealtDamage) {
            return;
        }

        gameData.forEachPermanent((controllerId, source) -> {
            if (controllerId.equals(damagedPlayerId)
                    || source.isLosesAllAbilitiesUntilEndOfTurn()
                    || gameQueryService.hasLostPrintedAbilities(gameData, source)) {
                return;
            }

            List<CardEffect> effects = new ArrayList<>(CombatWatch.ON_GOADED_CREATURES_COMBAT_DAMAGE_TO_OPPONENT.effectsOf(source.getCard()));
            effects.addAll(source.getTemporaryTriggerEffects(CombatWatch.ON_GOADED_CREATURES_COMBAT_DAMAGE_TO_OPPONENT.trigger()));
            effects.addAll(source.getPersistentTriggerEffects(CombatWatch.ON_GOADED_CREATURES_COMBAT_DAMAGE_TO_OPPONENT.trigger()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source, CombatWatch.ON_GOADED_CREATURES_COMBAT_DAMAGE_TO_OPPONENT.trigger()));
            Permanent matchingDealer = damageDealers.stream()
                    .filter(creature -> gameQueryService.isGoaded(gameData, creature))
                    .findFirst()
                    .orElse(null);
            if (matchingDealer == null) {
                return;
            }
            UUID dealerControllerId = gameQueryService.findPermanentController(gameData, matchingDealer.getId());
            for (CardEffect effect : effects) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        controllerId,
                        source.getCard().getName() + "'s triggered ability",
                        List.of(effect),
                        null,
                        source.getId());
                entry.setNonTargeting(true);
                int previousCopies = beginCombatDamageTriggerCopies(
                        gameData, controllerId, source, matchingDealer, dealerControllerId, 1);
                try {
                    gameData.enqueueTrigger(entry);
                    gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
                }
                log.info("Game {} - {} triggers when a goaded creature deals combat damage to an opponent",
                        gameData.id, source.getCard().getName());
            }
        });
    }

    public void checkEmblemAllyCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature,
                                                                     UUID creatureControllerId, int damageDealt) {
        if (creature == null || creatureControllerId == null || damageDealt <= 0) {
            return;
        }

        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(creatureControllerId)) {
                continue;
            }
            for (CardEffect effect : emblem.staticEffects()) {
                if (!(effect instanceof SearchBlueOrBlackCreatureToBattlefieldOnAllyCombatDamageEffect)) {
                    continue;
                }
                Card source = emblem.sourceCard();
                Card sourceCard = source != null ? source : creature.getCard();
                String description = (source != null ? source.getName() : "Emblem") + "'s emblem";
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        sourceCard,
                        emblem.controllerId(),
                        description,
                        List.of(new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardAnyOfPredicate(List.of(
                                                new CardColorPredicate(CardColor.BLUE),
                                                new CardColorPredicate(CardColor.BLACK))))),
                                LibrarySearchDestination.BATTLEFIELD)));
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.text(description + " triggers."));
                log.info("Game {} - {} combat-damage search trigger queued", gameData.id, description);
            }
        }
    }

    public void checkSourceDealsDamageToPlayerTriggers(GameData gameData, Permanent source,
                                                       UUID controllerId, UUID damagedPlayerId,
                                                       int damageDealt) {
        if (source == null || controllerId == null || damagedPlayerId == null || damageDealt <= 0) {
            return;
        }

        List<CardEffect> effects = new ArrayList<>(DamageWatch.ON_DAMAGE_TO_PLAYER.effectsOf(source.getCard()));
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                gameData, source, DamageWatch.ON_DAMAGE_TO_PLAYER.trigger()));
        if (!controllerId.equals(damagedPlayerId)) {
            effects.addAll(DamageWatch.ON_DAMAGE_TO_OPPONENT.effectsOf(source.getCard()));
            effects.addAll(source.getTemporaryTriggerEffects(DamageWatch.ON_DAMAGE_TO_OPPONENT.trigger()));
            effects.addAll(source.getPersistentTriggerEffects(DamageWatch.ON_DAMAGE_TO_OPPONENT.trigger()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source, DamageWatch.ON_DAMAGE_TO_OPPONENT.trigger()));
        }
        // An effect that removes all abilities removes the source's own triggered abilities,
        // so damage dealt after that loss does not trigger them.
        if (gameQueryService.hasLostAllAbilities(gameData, source)) {
            effects.clear();
        }
        for (CardEffect effect : effects) {
            queueNoncombatDamageToPlayerEffect(gameData, source, controllerId, damagedPlayerId, damageDealt, effect);
        }

        // Auras/Equipment attached to the source with ON_DAMAGE_TO_PLAYER (e.g. Curiosity).
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!perm.isAttached() || perm.getAttachedTo() == null
                    || !perm.getAttachedTo().equals(source.getId())) {
                return;
            }
            List<CardEffect> attachedEffects = new ArrayList<>(DamageWatch.ON_DAMAGE_TO_PLAYER.effectsOf(perm.getCard()));
            if (!ownerId.equals(damagedPlayerId)) {
                attachedEffects.addAll(DamageWatch.ON_DAMAGE_TO_OPPONENT.effectsOf(perm.getCard()));
            }
            for (CardEffect effect : attachedEffects) {
                // Attached triggers are controlled by the Aura/Equipment's controller.
                UUID attachedControllerId = gameData.findControllerOf(perm);
                if (attachedControllerId == null) continue;
                queueNoncombatDamageToPlayerEffect(gameData, perm, attachedControllerId, damagedPlayerId,
                        damageDealt, effect);
            }
        });
    }

    private void queueNoncombatDamageToPlayerEffect(GameData gameData, Permanent source, UUID controllerId,
                                                    UUID damagedPlayerId, int damageDealt, CardEffect effect) {
        CardEffect toQueue = effect;
        if (toQueue instanceof DamagedPlayerOnlyEffect onlyDamagedPlayer) {
            if (!damagedPlayerId.equals(onlyDamagedPlayer.playerId())) {
                return;
            }
            toQueue = onlyDamagedPlayer.wrapped();
        }
        if (toQueue instanceof ConditionalEffect conditional) {
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forPermanent(source, controllerId))) {
                return;
            }
            toQueue = conditional.wrapped();
        }

        if (toQueue instanceof LoseGameIfSourceDealtDamageToPlayerThisTurnEffect threshold
                && gameData.damageDealtBySourceToPlayerThisTurn(
                source.getId(), damagedPlayerId) < threshold.minimumDamage()) {
            return;
        }
        CardEffect damageEffect = toQueue instanceof MayEffect may ? may.wrapped() : toQueue;
        if (damageEffect instanceof TapChosenPermanentEffect tap
                && tap.chooseFromDamagedPlayer()) {
            CardEffect targeted = tap.forDamagedPlayer(damagedPlayerId);
            if (toQueue instanceof MayEffect may) {
                targeted = new MayEffect(targeted, may.prompt(), may.elseEffect(), may.choicePlayer());
            }
            gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                    source.getCard(), controllerId, List.of(targeted),
                    source.getId(), controllerId, damagedPlayerId));
            return;
        }
        if (damageEffect instanceof DamagedPlayerControlsTargetEffect damageTargetEffect) {
            if (damageDealt >= damageTargetEffect.minimumDamage()) {
                CardEffect targeted = damageTargetEffect.forDamagedPlayer(damagedPlayerId, damageDealt);
                if (toQueue instanceof MayEffect may) {
                    targeted = new MayEffect(targeted, may.prompt(), may.elseEffect(), may.choicePlayer());
                }
                gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                        source.getCard(), controllerId, List.of(targeted),
                        source.getId(), damageTargetEffect.targetChooserId(damagedPlayerId, controllerId),
                        damagedPlayerId));
            }
            return;
        }

        if (toQueue instanceof com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect bounce
                && bounce.fixedCount() == 0) {
            gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                    source.getCard(), controllerId, List.of(bounce.withTriggerTimeTargets()),
                    source.getId(), controllerId, damagedPlayerId, null, damageDealt));
            return;
        }

        if (toQueue.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    source.getCard(), controllerId, List.of(toQueue)));
            gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
            return;
        }

        if (toQueue instanceof MayEffect may) {
            int mayEventValue = may.wrapped() instanceof DrawCardEffect draw
                    && draw.amount() instanceof com.github.laxika.magicalvibes.model.amount.EventValue
                    ? damageDealt : 0;
            UUID mayTargetId = may.wrapped().targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    ? null : damagedPlayerId;
            gameData.queueMayAbility(source.getCard(), controllerId, may, mayTargetId, source.getId(), mayEventValue);
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage trigger fires."));
            return;
        }

        if (toQueue.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                    source.getCard(), controllerId, List.of(toQueue),
                    source.getId(), controllerId, damagedPlayerId));
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    "'s damage trigger goes on the stack — choose a target."));
            return;
        }

        String desc = source.getCard().getName() + "'s triggered ability";
        StackEntry se;
        CombatDamageTriggerContextEffect.TriggerContext triggerContext =
                toQueue instanceof CombatDamageTriggerContextEffect contextEffect
                        ? contextEffect.combatDamageTriggerContext()
                        : null;
        if (triggerContext == CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER_WITH_DAMAGE_AMOUNT) {
            se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    desc, List.of(toQueue), damageDealt, damagedPlayerId, null);
        } else if (triggerContext == CombatDamageTriggerContextEffect.TriggerContext.SOURCE_SELF) {
            se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    desc, List.of(toQueue), null, source.getId());
        } else if (triggerContext == CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER) {
            se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    desc, List.of(toQueue), damagedPlayerId, source.getId());
        } else {
            se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                    desc, List.of(toQueue));
        }
        if (toQueue instanceof DiscardEffect
                || (toQueue instanceof DrawCardEffect draw
                        && draw.amount() instanceof com.github.laxika.magicalvibes.model.amount.EventValue)
                || (toQueue instanceof MillEffect mill
                        && mill.count() instanceof com.github.laxika.magicalvibes.model.amount.EventValue)) {
            se.setEventValue(damageDealt);
        }
        se.setNonTargeting(true);
        gameData.stack.add(se);
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage trigger goes on the stack."));
    }

    // ── Damage-dealt-to-controller triggers ────────────────────────────

    public void checkDamageDealtToControllerTriggers(GameData gameData, UUID damagedPlayerId, UUID sourcePermanentId, boolean isCombatDamage) {
        checkDamageDealtToControllerTriggers(gameData, damagedPlayerId, sourcePermanentId, isCombatDamage, null);
    }

    public void checkDamageDealtToControllerTriggers(GameData gameData, UUID damagedPlayerId,
                                                     UUID sourcePermanentId, boolean isCombatDamage,
                                                     Set<String> firedBatchedTriggerKeys) {
        if (sourcePermanentId == null) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        boolean hasPlanarCombatDamageTrigger = isCombatDamage
                && hasPlanarCreatureCombatDamageToYouTrigger(gameData, damagedPlayerId);
        boolean hasPlanarDamageTrigger = hasPlanarPermanentDamageToYouTrigger(gameData, damagedPlayerId);
        if (damagedPlayerBattlefield == null && !hasPlanarCombatDamageTrigger && !hasPlanarDamageTrigger) return;

        boolean hasTrigger = false;
        if (damagedPlayerBattlefield != null) {
            for (Permanent perm : gameData.permanentsAndFaceUpSchemes(damagedPlayerId)) {
                if (!DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU.effectsOf(perm.getCard()).isEmpty()
                        || (isCombatDamage
                                && !CombatWatch.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU.effectsOf(perm.getCard()).isEmpty())) {
                    hasTrigger = true;
                    break;
                }
            }
        }
        if (!hasTrigger && !hasPlanarCombatDamageTrigger && !hasPlanarDamageTrigger) return;

        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) return;

        checkPlanarPermanentDealsDamageToYouTriggers(gameData, damagedPlayerId, sourcePermanent, isCombatDamage);

        if (isCombatDamage) {
            if (damagedPlayerBattlefield != null) {
                queueCreatureCombatDamageToYouTriggers(
                        gameData, damagedPlayerId, sourcePermanent, firedBatchedTriggerKeys);
            }
            queuePlanarCreatureCombatDamageToYouTriggers(gameData, damagedPlayerId, sourcePermanent);
        }

        if (damagedPlayerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToController(damagedPlayerId, sourcePermanentId, isCombatDamage);

        for (Permanent perm : new ArrayList<>(gameData.permanentsAndFaceUpSchemes(damagedPlayerId))) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
            for (CardEffect effect : DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU.effectsOf(perm.getCard())) {
                var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                boolean triggered = dispatch(match, DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

                // If the source was bounced, stop processing all triggers
                if (triggered && gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
                    return;
                }
            }
        }
    }

    /**
     * Puts every {@link CombatWatch#ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU} ability on the damaged
     * player's battlefield and face-up schemes (CR 314.4) onto the stack when a creature dealt them combat damage. The whole slot
     * becomes one triggered ability per watching permanent whose {@code targetId} is the damaging
     * creature, so "destroy that creature" is expressed with {@code DestroyTargetPermanentEffect}
     * while the ability itself does not target (CR 115.10a).
     */
    private void queueCreatureCombatDamageToYouTriggers(GameData gameData, UUID damagedPlayerId,
                                                        Permanent sourceCreature,
                                                        Set<String> firedBatchedTriggerKeys) {
        if (!gameQueryService.isCreature(gameData, sourceCreature)) return;

        for (Permanent perm : new ArrayList<>(gameData.permanentsAndFaceUpSchemes(damagedPlayerId))) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
            List<CardEffect> effects = new ArrayList<>();
            List<CardEffect> authoredEffects = CombatWatch.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU.effectsOf(perm.getCard());
            for (int effectIndex = 0; effectIndex < authoredEffects.size(); effectIndex++) {
                CardEffect effect = authoredEffects.get(effectIndex);
                String triggerKey = perm.getId() + ":" + effectIndex;
                CardEffect resolved = resolveTriggeringPermanentConditional(
                        gameData, perm, damagedPlayerId, sourceCreature, effect);
                if (resolved instanceof ConditionalEffect conditional && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(perm, damagedPlayerId))) {
                    continue;
                }
                if (resolved != null) {
                    boolean batchedCombatTrigger = resolved instanceof BatchedCombatDamageToYouTriggerEffect;
                    if (resolved instanceof BatchedCombatDamageToYouTriggerEffect batchedEffect) {
                        if (firedBatchedTriggerKeys != null
                                && firedBatchedTriggerKeys.contains(triggerKey)) {
                            continue;
                        }
                        resolved = batchedEffect.wrapped();
                        if (firedBatchedTriggerKeys != null) {
                            firedBatchedTriggerKeys.add(triggerKey);
                        }
                    }
                    if (resolved instanceof DamageSourceControllerGainsControlOfThisPermanentEffect control) {
                        UUID recipientId = batchedCombatTrigger ? gameData.activePlayerId
                                : gameQueryService.findPermanentController(gameData, sourceCreature.getId());
                        if (recipientId != null
                                && (batchedCombatTrigger || !recipientId.equals(damagedPlayerId))) {
                            effects.add(new DamageSourceControllerGainsControlOfDamagedPermanentEffect(
                                    recipientId));
                            if (control.untap()) {
                                effects.add(new UntapPermanentsEffect(TapUntapScope.SELF));
                            }
                        }
                        continue;
                    }
                    effects.add(resolved);
                }
            }
            if (effects.isEmpty()) continue;

            StackEntry se = new StackEntry(StackEntryType.TRIGGERED_ABILITY, perm.getCard(), damagedPlayerId,
                    perm.getCard().getName() + "'s triggered ability", List.copyOf(effects),
                    sourceCreature.getId(), perm.getId());
            se.setNonTargeting(true);
            se.setSourcePermanentSnapshot(new Permanent(perm));
            se.setTriggeringPermanentId(sourceCreature.getId());
            se.setTriggeringPermanentControllerId(
                    gameQueryService.findPermanentController(gameData, sourceCreature.getId()));
            gameData.stack.add(se);
            gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                    "'s combat damage trigger goes on the stack."));
        }
    }

    // ── Enchanted-creature-deals-damage-to-you reflect triggers (Backfire) ──

    /**
     * Handles {@link DamageWatch#ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU} — "Whenever enchanted creature
     * deals damage to you, this Aura deals that much damage to that creature's controller" (Backfire).
     *
     * <p>The aura is on its controller's battlefield, so scanning the damaged player's battlefield for auras
     * attached to the damage source naturally restricts the trigger to damage dealt to the aura's controller
     * ("to you"). Called for both combat and non-combat damage dealt to a player. Queues a triggered ability
     * running {@link EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect}, whose handler deals
     * {@code amount} damage to the enchanted creature's controller.
     */
    public void checkEnchantedCreatureDealtDamageToControllerReflectTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourcePermanentId, int amount) {
        if (sourcePermanentId == null || amount <= 0) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (enchantedCreature == null) return;

        for (Permanent aura : new ArrayList<>(damagedPlayerBattlefield)) {
            if (!aura.isAttached() || !sourcePermanentId.equals(aura.getAttachedTo())) continue;
            if (DamageWatch.ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU.effectsOf(aura.getCard()).isEmpty()) continue;

            UUID creatureControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
            if (creatureControllerId == null) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    aura.getCard(),
                    damagedPlayerId,
                    aura.getCard().getName() + "'s ability",
                    new ArrayList<>(DamageWatch.ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU.effectsOf(aura.getCard())),
                    amount,
                    creatureControllerId,
                    aura.getId(),
                    Map.of(),
                    null,
                    List.of(),
                    List.of()
            );
            entry.setDamageSourceCard(enchantedCreature.getCard());
            entry.setTriggeringPermanentId(enchantedCreature.getId());
            entry.setNonTargeting(true);
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.abilityTriggers(aura.getCard()));
            log.info("Game {} - {} ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU trigger fires ({} damage)",
                    gameData.id, aura.getCard().getName(), amount);
        }
    }

    // ── Controller-dealt-damage triggers (Living Artifact) ─────────────

    /**
     * Handles {@link DamageWatch#ON_CONTROLLER_DEALT_DAMAGE} — "Whenever you're dealt damage, ...".
     * Fires once per damage source (per the CR ruling that simultaneous sources trigger separately),
     * carrying only the amount so an {@code EventValue} amount ("put that many counters") can read it.
     * Scans the damaged player's own battlefield.
     * <p>
     * Also handles {@link DamageWatch#ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT} — "Whenever a source an
     * opponent controls deals damage to you, ..." (Retaliator Griffin) — but only when the damage
     * source is controlled by an opponent of the damaged player. {@code sourceControllerId} is the
     * controller of the damage source (the active player for combat damage to the defender, the
     * spell/ability's controller for non-combat damage); {@code null} disables the opponent-gated slot.
     */
    public void checkControllerDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        checkControllerDealtDamageTriggers(gameData, damagedPlayerId, sourceControllerId, amount, true);
    }

    public void checkControllerDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount, boolean includeAllSources) {
        if (amount <= 0) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, null,
                sourceControllerId);
        boolean fromOpponent = sourceControllerId != null && !sourceControllerId.equals(damagedPlayerId);
        boolean fromAllySource = sourceControllerId != null && sourceControllerId.equals(damagedPlayerId);

        for (Permanent perm : List.copyOf(damagedPlayerBattlefield)) {
            for (CardEffect effect : includeAllSources
                    ? DamageWatch.ON_CONTROLLER_DEALT_DAMAGE.effectsOf(perm.getCard()) : List.<CardEffect>of()) {
                var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                dispatch(match, DamageWatch.ON_CONTROLLER_DEALT_DAMAGE, effect, ctx);
            }
            if (fromAllySource) {
                for (CardEffect effect : DamageWatch.ON_CONTROLLER_DEALT_DAMAGE_BY_ALLY_SOURCE.effectsOf(perm.getCard())) {
                    boolean oncePerTurn = effect instanceof OncePerTurnTriggerEffect;
                    CardEffect resolved = unwrapOncePerTurnTrigger(gameData, perm, effect);
                    if (resolved == null) continue;
                    var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                    if (dispatch(match, DamageWatch.ON_CONTROLLER_DEALT_DAMAGE_BY_ALLY_SOURCE,
                            resolved, ctx) && oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                    }
                }
            }
            if (fromOpponent) {
                for (CardEffect effect : DamageWatch.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT.effectsOf(perm.getCard())) {
                    var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                    dispatch(match, DamageWatch.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT, effect, ctx);
                }
            }
        }
    }

    /**
     * Handles {@link DamageWatch#ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT} — "Whenever a source you
     * control deals damage to another player, ..." (Night Dealings). The outbound mirror of
     * {@link #checkControllerDealtDamageTriggers}: it scans the damage source's controller's own
     * battlefield, and only fires when the damaged player is someone else.
     * The source permanent ID is carried for effects that are self-scoped within this slot.
     */
    public void checkAllySourceDealtDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        checkAllySourceDealtDamageToOpponentTriggers(gameData, damagedPlayerId, sourceControllerId, null, amount);
    }

    public void checkAllySourceDealtNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        if (amount <= 0 || sourceControllerId == null || sourceControllerId.equals(damagedPlayerId)) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;
        var ctx = new TriggerContext.NoncombatDamageToOpponent(damagedPlayerId, sourceControllerId, amount);
        for (Permanent permanent : List.copyOf(battlefield)) {
            for (CardEffect effect : DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT.effectsOf(permanent.getCard())) {
                var match = new TriggerMatchContext(gameData, permanent, sourceControllerId, effect);
                dispatch(match, DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT, effect, ctx);
            }
        }
    }

    public void checkAllySourceDealtDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, UUID sourcePermanentId, int amount) {
        if (amount <= 0 || sourceControllerId == null) return;

        if (!sourceControllerId.equals(damagedPlayerId)) {
            checkPlanarAllySourceDealtDamageToOpponentTriggers(
                    gameData, damagedPlayerId, sourceControllerId, sourcePermanentId, amount);
        } else {
            checkPlanarSourceDealtDamageToItsControllerTriggers(
                    gameData, damagedPlayerId, sourcePermanentId, amount);
        }

        List<Permanent> sourceControllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (sourceControllerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, sourcePermanentId);
        for (Permanent perm : List.copyOf(sourceControllerBattlefield)) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
            for (DamageWatch watch : List.of(DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT,
                    DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_PLAYER)) {
                if (watch == DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT
                        && sourceControllerId.equals(damagedPlayerId)) continue;
                for (CardEffect effect : watch.effectsOf(perm.getCard())) {
                    var match = new TriggerMatchContext(gameData, perm, sourceControllerId, effect);
                    dispatch(match, watch, effect, ctx);
                }
            }
        }
    }

    /**
     * Handles effects that trigger whenever an opponent is dealt damage, regardless of the damage
     * source's controller.
     */
    public void checkOpponentDealtDamageTriggers(GameData gameData, UUID damagedPlayerId, int amount) {
        checkOpponentDealtDamageTriggers(gameData, damagedPlayerId, null, amount);
    }

    public void checkOpponentDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourcePermanentId, int amount) {
        if (amount <= 0 || damagedPlayerId == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, sourcePermanentId);
        gameData.forEachBattlefield((watcherPlayerId, battlefield) -> {
            if (watcherPlayerId.equals(damagedPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : DamageWatch.ON_OPPONENT_DEALT_DAMAGE.effectsOf(perm.getCard())) {
                    var match = new TriggerMatchContext(gameData, perm, watcherPlayerId, effect);
                    registry.dispatch(match, DamageWatch.ON_OPPONENT_DEALT_DAMAGE, effect, ctx);
                }
            }
        });
    }

    /**
     * Handles {@link DamageWatch#ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT} — "Whenever a
     * creature of the chosen color deals damage to you or a white creature you control, ...".
     * Scans the damaged player's battlefield for watchers; the per-watcher chosen-color and
     * damaged-permanent filtering happens in the dispatched collector.
     *
     * @param damagedPlayerId  the damaged player, or the damaged permanent's controller
     * @param damagedPermanent the damaged permanent, or {@code null} when the player was damaged
     */
    public void checkCreatureDamageToYouOrYourPermanentTriggers(GameData gameData, UUID damagedPlayerId,
            Permanent damagedPermanent, Permanent damageSource, int damage) {
        if (damage <= 0 || damagedPlayerId == null || damageSource == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.CreatureDamageToYouOrYourPermanent(
                damageSource, damagedPlayerId, damagedPermanent, damage);
        for (Permanent perm : List.copyOf(battlefield)) {
            for (CardEffect effect : DamageWatch.ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT.effectsOf(perm.getCard())) {
                var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                dispatch(match, DamageWatch.ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT, effect, ctx);
            }
        }
    }

    /**
     * Fires triggers for each player or permanent recipient of damage during one damage event.
     * A permanent recipient is retained separately from its controller because cards may trigger
     * once for each permanent damaged, in addition to a trigger for damage dealt to the player.
     */
    public void checkOpponentSourceDamageToYouOrYourPermanentTriggers(GameData gameData, Card sourceCard,
            UUID sourceControllerId, UUID sourcePermanentId, Set<UUID> damagedPlayerIds) {
        if (sourceCard == null || sourceControllerId == null || damagedPlayerIds == null
                || damagedPlayerIds.isEmpty()) return;

        List<SourceDamageRecipient> recipients = damagedPlayerIds.stream()
                .filter(Objects::nonNull)
                .map(playerId -> new SourceDamageRecipient(playerId, null))
                .toList();
        checkOpponentSourceDamageToYouOrYourPermanentTriggers(
                gameData, sourceCard, sourceControllerId, sourcePermanentId, recipients);
    }

    public void checkOpponentSourceDamageToYouOrYourPermanentTriggers(GameData gameData, Card sourceCard,
            UUID sourceControllerId, UUID sourcePermanentId, Collection<SourceDamageRecipient> recipients) {
        if (sourceCard == null || sourceControllerId == null || recipients == null || recipients.isEmpty()) return;

        for (SourceDamageRecipient recipient : recipients) {
            if (recipient == null || recipient.damagedPlayerId() == null) continue;
            UUID damagedPlayerId = recipient.damagedPlayerId();
            List<Permanent> battlefield = gameData.playerBattlefields.get(damagedPlayerId);
            if (battlefield == null) continue;

            TriggerContext.SourceDamageToYouOrYourPermanent ctx =
                    new TriggerContext.SourceDamageToYouOrYourPermanent(
                            sourceCard, sourceControllerId, sourcePermanentId, damagedPlayerId,
                            recipient.damagedPermanentId());
            for (Permanent perm : List.copyOf(battlefield)) {
                for (CardEffect effect : DamageWatch.ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT.effectsOf(perm.getCard())) {
                    var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                    dispatch(match,
                            DamageWatch.ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT,
                            effect, ctx);
                }
            }
        }
    }

    public record SourceDamageRecipient(UUID damagedPlayerId, UUID damagedPermanentId) {
    }

    // ── Any-source-deals-damage triggers (Justice) ─────────────────────

    /**
     * Handles {@link DamageWatch#ON_ANY_SOURCE_DEALS_DAMAGE} — "Whenever a [color] creature or spell
     * deals damage, ...". Scans every battlefield for permanents with this slot and dispatches the
     * batched damage event (already summed across simultaneous targets) so each watcher can react
     * once. Callers pass the single summed total per source per damage event.
     */
    public void checkAllyInstantOrSorcerySpellDealsDamageTriggers(GameData gameData,
                                                                   PendingSourceDamage batch) {
        Card sourceCard = batch.getSourceCard();
        UUID sourceControllerId = batch.getControllerId();
        if (!sourceCard.hasType(CardType.INSTANT) && !sourceCard.hasType(CardType.SORCERY)) {
            return;
        }
        var context = new TriggerContext.SourceDealsDamage(sourceCard, sourceControllerId,
                batch.getSourcePermanentId(), batch.getAmount(), batch.getDamageToPlayers(),
                batch.getSingleCreatureSpellTargetId(), batch.getDamageToPermanents(), false);
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;
        for (Permanent permanent : List.copyOf(battlefield)) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, permanent)) continue;
            for (CardEffect effect : DamageWatch.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE.effectsOf(permanent.getCard())) {
                var match = new TriggerMatchContext(gameData, permanent, sourceControllerId, effect);
                dispatch(match, DamageWatch.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE, effect, context);
            }
        }
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, Map.of());
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, null);
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers,
                                                   List<CardEffect> snapshottedSelfEffects) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId, totalDamage,
                damageToPlayers, snapshottedSelfEffects, null, Map.of());
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers,
                                                   List<CardEffect> snapshottedSelfEffects,
                                                   UUID singleCreatureSpellTargetId,
                                                   Map<UUID, Integer> damageToPermanents) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId, totalDamage,
                damageToPlayers, snapshottedSelfEffects, singleCreatureSpellTargetId, damageToPermanents, false);
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers,
                                                   List<CardEffect> snapshottedSelfEffects,
                                                   UUID singleCreatureSpellTargetId,
                                                   Map<UUID, Integer> damageToPermanents,
                                                   boolean combatDamage) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, snapshottedSelfEffects, singleCreatureSpellTargetId,
                damageToPermanents, combatDamage, Map.of());
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers,
                                                   List<CardEffect> snapshottedSelfEffects,
                                                   UUID singleCreatureSpellTargetId,
                                                   Map<UUID, Integer> damageToPermanents,
                                                   boolean combatDamage,
                                                   Map<UUID, Permanent> damagedPermanentSnapshots) {
        if (sourceCard == null || sourceControllerId == null || totalDamage <= 0) return;

        var ctx = new TriggerContext.SourceDealsDamage(sourceCard, sourceControllerId, sourcePermanentId, totalDamage,
                damageToPlayers == null ? Map.of() : Map.copyOf(damageToPlayers), singleCreatureSpellTargetId,
                damageToPermanents == null ? Map.of() : Map.copyOf(damageToPermanents), combatDamage,
                damagedPermanentSnapshots);
        gameData.forEachBattlefield((watcherPlayerId, battlefield) -> {
            for (Permanent perm : List.copyOf(battlefield)) {
                if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                for (CardEffect effect : DamageWatch.ON_ANY_SOURCE_DEALS_DAMAGE.effectsOf(perm.getCard())) {
                    var match = new TriggerMatchContext(gameData, perm, watcherPlayerId, effect);
                    dispatch(match, DamageWatch.ON_ANY_SOURCE_DEALS_DAMAGE, effect, ctx);
                }
            }
        });

        // Self triggers (El-Hajjâj): only the damage source's own "whenever this creature deals
        // damage" abilities fire. Keyed off the source card (not a battlefield scan) so it still
        // triggers when the source died dealing that damage.
        Permanent sourcePermanent = findPermanentByCardId(gameData, sourceCard.getId());
        boolean sourceHasPrintedAbilities = sourcePermanent == null
                || !sourcePermanent.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, sourcePermanent);
        List<CardEffect> selfEffects = snapshottedSelfEffects != null
                ? new ArrayList<>(snapshottedSelfEffects)
                : sourceHasPrintedAbilities
                ? new ArrayList<>(DamageWatch.ON_SELF_DEALS_DAMAGE.effectsOf(sourceCard))
                : new ArrayList<>();
        // Granted "whenever this creature deals damage" abilities live on the permanent, not the card
        // (the Genju cycle grants one to the animated land until end of turn). If no snapshot was
        // captured, the source is still on the battlefield and can be read here.
        if (snapshottedSelfEffects == null && sourcePermanent != null) {
            selfEffects.addAll(sourcePermanent.getTemporaryTriggerEffects(DamageWatch.ON_SELF_DEALS_DAMAGE.trigger()));
            selfEffects.addAll(sourcePermanent.getPersistentTriggerEffects(DamageWatch.ON_SELF_DEALS_DAMAGE.trigger()));
            selfEffects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, sourcePermanent, DamageWatch.ON_SELF_DEALS_DAMAGE.trigger()));
        }
        for (CardEffect effect : selfEffects) {
            var match = new TriggerMatchContext(gameData, sourcePermanent, sourceControllerId, effect);
            dispatch(match, DamageWatch.ON_SELF_DEALS_DAMAGE, effect, ctx);
        }

        for (DelayedWatchedCreatureDealsDamage watch
                : gameData.getDelayedActions(DelayedWatchedCreatureDealsDamage.class)) {
            if (!watch.watchedPermanentId().equals(sourcePermanentId)) continue;

            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watch.sourceCard(),
                    watch.controllerId(),
                    watch.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(watch.effects()),
                    watch.watchedPermanentId(),
                    (UUID) null);
            trigger.setNonTargeting(true);
            trigger.setEventValue(totalDamage);
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
        }
    }

    /**
     * Fires delayed triggers watching a Wall for damage from an attacking creature. The source's
     * attacking status is checked at the damage event, so noncombat damage from a spell and damage
     * from a blocking creature do not qualify.
     */
    public void checkDelayedWatchedCreatureDealtDamageByAttackingCreatureTriggers(
            GameData gameData, Permanent damageSource, Permanent damagedCreature, int damage) {
        if (damageSource == null || damagedCreature == null || damage <= 0
                || !damageSource.isAttacking() || !gameQueryService.isCreature(gameData, damageSource)) {
            return;
        }

        for (DelayedWatchedCreatureDealtDamageByAttackingCreature watch
                : gameData.getDelayedActions(DelayedWatchedCreatureDealtDamageByAttackingCreature.class)) {
            if (!watch.watchedPermanentId().equals(damagedCreature.getId())) continue;

            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watch.sourceCard(),
                    watch.controllerId(),
                    watch.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(watch.effects()),
                    watch.watchedPermanentId(),
                    (UUID) null);
            trigger.setNonTargeting(true);
            trigger.setEventValue(damage);
            gameData.stack.add(trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
        }
    }

    public void queueEnchantedCreatureDealsDamageTriggers(GameData gameData, Permanent sourceCreature,
                                                           int damageDealt) {
        if (sourceCreature == null || damageDealt <= 0) return;

        List<StackEntry> entries = new ArrayList<>();
        collectEnchantedCreatureDealsDamageTriggers(gameData, sourceCreature, damageDealt, entries);
        entries.forEach(gameData::enqueueTrigger);
    }

    public void collectEnchantedCreatureDealsDamageTriggers(GameData gameData, Permanent sourceCreature,
                                                             int damageDealt, List<StackEntry> entries) {
        if (sourceCreature == null || damageDealt <= 0 || entries == null) return;

        gameData.forEachPermanent((auraControllerId, aura) -> {
            if (!aura.isAttached() || !sourceCreature.getId().equals(aura.getAttachedTo())) return;

            List<CardEffect> effects = DamageWatch.ON_ENCHANTED_CREATURE_DEALS_DAMAGE.effectsOf(aura.getCard());
            if (effects.isEmpty()) return;

            UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourceCreature.getId());
            List<CardEffect> boundEffects = effects.stream()
                    .map(effect -> effect instanceof DamageSourceControllerLosesLifeEffect loss
                            ? loss.bindCurrentControllerAtResolution(sourceControllerId, damageDealt)
                            : effect instanceof DamageSourceControllerAwareEffect aware
                            ? aware.bindDamageSourceController(sourceControllerId, damageDealt)
                            : effect)
                    .toList();

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    aura.getCard(),
                    auraControllerId,
                    aura.getCard().getName() + "'s ability",
                    new ArrayList<>(boundEffects),
                    auraControllerId,
                    aura.getId());
            entry.setEventValue(damageDealt);
            entry.setTriggeringPermanentId(sourceCreature.getId());
            entry.setSourcePermanentSnapshot(new Permanent(aura));
            entry.setNonTargeting(true);
            entries.add(entry);
        });
    }

    /**
     * Collects Aura triggers for a creature dealing damage to another creature. The damaged
     * creature is captured before state-based actions run, so the trigger still exists when either
     * combat creature dies from the same damage event.
     */
    public void collectEnchantedCreatureDealsDamageToCreatureTriggers(GameData gameData,
                                                                        Permanent sourceCreature,
                                                                        UUID damagedCreatureId,
                                                                        int damageDealt,
                                                                        List<StackEntry> entries) {
        if (sourceCreature == null || damagedCreatureId == null || damageDealt <= 0
                || sourceCreature.getId().equals(damagedCreatureId)
                || !gameQueryService.isCreature(gameData, sourceCreature)
                || entries == null) {
            return;
        }

        gameData.forEachPermanent((auraControllerId, aura) -> {
            if (!aura.isAttached() || !sourceCreature.getId().equals(aura.getAttachedTo())) return;

            List<CardEffect> effects = DamageWatch.ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_CREATURE.effectsOf(aura.getCard());
            if (effects.isEmpty()) return;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    aura.getCard(),
                    auraControllerId,
                    aura.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    damagedCreatureId,
                    aura.getId());
            entry.setNonTargeting(true);
            entries.add(entry);
        });
    }

    public void queueEnchantedCreatureDealsDamageToCreatureTriggers(GameData gameData,
                                                                      Permanent sourceCreature,
                                                                      UUID damagedCreatureId,
                                                                      int damageDealt) {
        List<StackEntry> entries = new ArrayList<>();
        collectEnchantedCreatureDealsDamageToCreatureTriggers(
                gameData, sourceCreature, damagedCreatureId, damageDealt, entries);
        entries.forEach(gameData::enqueueTrigger);
    }

    /** The battlefield permanent whose card has this id, or null once it has left the battlefield. */
    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        List<Permanent> found = new ArrayList<>(1);
        gameData.forEachPermanent((playerId, perm) -> {
            if (perm.getCard().getId().equals(cardId)) {
                found.add(perm);
            }
        });
        return found.isEmpty() ? null : found.getFirst();
    }

    // ── Land-tap triggers ──────────────────────────────────────────────

    /**
     * Dispatches combat-damage-only self triggers from the source card. The source permanent ID is
     * retained even when state-based actions removed the source before triggered abilities were put
     * on the stack, so effects that require the source to have survived can check it at resolution.
     */
    public void queueSourceDealsCombatDamageTriggers(GameData gameData, Card sourceCard,
                                                      UUID sourceControllerId, UUID sourcePermanentId,
                                                      int totalDamage, int damageToPlayers,
                                                      UUID damagedPlayerId,
                                                      List<CardEffect> snapshottedSelfEffects) {
        queueSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, damagedPlayerId, snapshottedSelfEffects, null);
    }

    /**
     * Captures, per controller, the permanents with "whenever a creature you control deals combat
     * damage" abilities before state-based actions remove them: the event happens while they are
     * still on the battlefield (CR 603.2), so one destroyed by that same damage still triggers.
     */
    public Map<UUID, List<Permanent>> snapshotAllyCreatureDealsCombatDamageWatchers(GameData gameData) {
        Map<UUID, List<Permanent>> watchers = new LinkedHashMap<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!CombatWatch.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE.effectsOf(permanent.getCard()).isEmpty()) {
                watchers.computeIfAbsent(controllerId, ignored -> new ArrayList<>()).add(permanent);
            }
        });
        return watchers;
    }

    public void queueSourceDealsCombatDamageTriggers(GameData gameData, Card sourceCard,
                                                      UUID sourceControllerId, UUID sourcePermanentId,
                                                      int totalDamage, int damageToPlayers,
                                                      UUID damagedPlayerId,
                                                      List<CardEffect> snapshottedSelfEffects,
                                                      List<Permanent> snapshottedAllyWatchers) {
        if (sourceCard == null || sourceControllerId == null || sourcePermanentId == null || totalDamage <= 0) {
            return;
        }

        var ctx = new TriggerContext.SourceDealsCombatDamage(
                sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers, damagedPlayerId);
        dispatchSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, snapshottedSelfEffects, CombatWatch.ON_SELF_DEALS_COMBAT_DAMAGE);

        // "Whenever a creature you control deals combat damage" watchers (Five-Alarm Fire). Scanned on
        // the damage source's controller's battlefield only; the watcher itself needn't be a creature.
        // The damage source counts as a creature as it currently is, so an animated noncreature
        // permanent (Gruul Keyrune) qualifies, while a printed creature that stopped being one does not.
        Permanent damageSource = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        boolean damageSourceIsCreature = damageSource != null
                ? gameQueryService.isCreature(gameData, damageSource)
                : sourceCard.hasType(CardType.CREATURE);
        if (!damageSourceIsCreature) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;
        List<Permanent> allyWatchers = snapshottedAllyWatchers != null
                ? snapshottedAllyWatchers : List.copyOf(battlefield);
        for (Permanent watcher : allyWatchers) {
            for (CardEffect effect : CombatWatch.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE.effectsOf(watcher.getCard())) {
                var match = new TriggerMatchContext(gameData, watcher, sourceControllerId, effect);
                dispatch(match, CombatWatch.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE, effect, ctx);
            }
        }

        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!watcher.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    || !isEquippedBy(gameData, watcher, sourcePermanentId)) {
                return;
            }
            if (damageToPlayers > 0) {
                for (CardEffect effect : CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER.effectsOf(watcher.getCard())) {
                    var match = new TriggerMatchContext(gameData, watcher, watcherControllerId, effect);
                    dispatch(match, CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER, effect, ctx);
                }
            }
            for (CardEffect effect : CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE.effectsOf(watcher.getCard())) {
                var match = new TriggerMatchContext(gameData, watcher, watcherControllerId, effect);
                dispatch(match, CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE, effect, ctx);
            }
        });
    }

    /** Dispatches the player-or-battle combat-damage trigger on Equipment attached to the source. */
    private void queueEquippedCreatureDealsCombatDamageToPlayerOrBattleTriggers(GameData gameData,
                                                                                  Card sourceCard,
                                                                                  UUID sourceControllerId,
                                                                                  UUID sourcePermanentId,
                                                                                  int totalDamage,
                                                                                  int damageToPlayers) {
        var ctx = new TriggerContext.SourceDealsCombatDamage(
                sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers);
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!watcher.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    || !isEquippedBy(gameData, watcher, sourcePermanentId)) {
                return;
            }
            for (CardEffect effect : CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE.effectsOf(watcher.getCard())) {
                var match = new TriggerMatchContext(gameData, watcher, watcherControllerId, effect);
                dispatch(match, CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE,
                        effect, ctx);
            }
        });
    }

    /**
     * Handles {@link TapWatch#ON_SELF_TAPPED_FOR_MANA} — "Whenever you tap this permanent for
     * mana, …" (Zhur-Taa Druid). Only the tapped permanent's own card is scanned, and only the
     * mana-ability tap path calls this, so tapping to attack or an opponent's forced tap never
     * triggers it. The caller defers the queued trigger like every other mana-ability trigger
     * (CR 603.3).
     *
     * @param gameData        the current game state
     * @param tappedPermanent the permanent that was just tapped for mana
     * @param controllerId    the controller of that permanent (also the player who tapped it)
     */
    public void checkSelfTappedForManaTriggers(GameData gameData, Permanent tappedPermanent, UUID controllerId) {
        List<CardEffect> effects = new ArrayList<>(
                TapWatch.ON_SELF_TAPPED_FOR_MANA.effectsOf(tappedPermanent.getCard()));
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                gameData, tappedPermanent, TapWatch.ON_SELF_TAPPED_FOR_MANA.trigger()));
        if (effects.isEmpty()) return;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                tappedPermanent.getCard(),
                controllerId,
                tappedPermanent.getCard().getName() + "'s ability",
                new ArrayList<>(effects),
                null,
                tappedPermanent.getId());
        entry.setTriggeringPermanentId(tappedPermanent.getId());
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(tappedPermanent.getCard()));
        log.info("Game {} - {} triggers on being tapped for mana", gameData.id, tappedPermanent.getCard().getName());
    }

    public void checkManaAbilityResolutionTriggers(GameData gameData, Permanent sourcePermanent,
                                                    UUID controllerId, int manaProduced) {
        List<CardEffect> effects = new ArrayList<>(
                SpellAbilityWatch.ON_SELF_MANA_ABILITY_RESOLVES.effectsOf(sourcePermanent.getCard()));
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                gameData, sourcePermanent, SpellAbilityWatch.ON_SELF_MANA_ABILITY_RESOLVES.trigger()));
        if (effects.isEmpty()) return;

        TriggerContext context = new TriggerContext.ManaAbilityResolved(controllerId, manaProduced);
        for (CardEffect effect : effects) {
            CardEffect resolved = unwrapOncePerTurnTrigger(gameData, sourcePermanent, effect);
            if (resolved == null) continue;
            boolean triggered = dispatch(new TriggerMatchContext(
                    gameData, sourcePermanent, controllerId, effect),
                    SpellAbilityWatch.ON_SELF_MANA_ABILITY_RESOLVES, resolved, context);
            if (triggered) {
                OncePerTurnTriggerSupport.markIfNeeded(gameData, sourcePermanent, effect);
            }
        }
    }

    public void checkLandTapTriggers(GameData gameData, UUID tappingPlayerId, UUID tappedLandId) {
        checkLandTapTriggers(gameData, tappingPlayerId, tappedLandId, Set.of());
    }

    public void checkLandTapTriggers(GameData gameData, UUID tappingPlayerId, UUID tappedLandId,
                                     Set<ManaColor> producedColors) {
        // Desolation et al.: track who tapped a land for mana this turn even if no land-tap
        // trigger permanent is currently on the battlefield (2004-10-04 ruling).
        gameData.playersWhoTappedLandForManaThisTurn.add(tappingPlayerId);

        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.LandTap(tappingPlayerId, tappedLandId, producedColors);

        // Snapshot each battlefield so trigger collection remains stable if a collector mutates it.
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : List.copyOf(battlefield)) {
                if (gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
                for (CardEffect effect : TapWatch.ON_ANY_PLAYER_TAPS_LAND.effectsOf(perm.getCard())) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    if (dispatch(match, TapWatch.ON_ANY_PLAYER_TAPS_LAND, effect, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        if (gameData.planechase != null && gameData.planechase.controllerId != null) {
            UUID planarControllerId = gameData.planechase.controllerId;
            for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
                for (CardEffect effect : TapWatch.ON_ANY_PLAYER_TAPS_LAND.effectsOf(object.getCard())) {
                    var match = new TriggerMatchContext(gameData, null, planarControllerId, effect,
                            object.getCard(), object.copy());
                    if (registry.dispatch(match, TapWatch.ON_ANY_PLAYER_TAPS_LAND, effect, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        }

        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : List.copyOf(gameData.floatingEffects)) {
                if (!(floating.effect() instanceof AddOneOfEachManaTypeProducedByLandEffect effect)
                        || floating.affectedPlayerId() == null) {
                    continue;
                }
                TriggerMatchContext match = new TriggerMatchContext(
                        gameData, null, floating.affectedPlayerId(), effect);
                if (registry.dispatch(match, TapWatch.ON_ANY_PLAYER_TAPS_LAND, effect, ctx)) {
                    anyTriggered[0] = true;
                }
            }
        }

        applyTurnScopedExtraLandTapMana(gameData, tappingPlayerId, tappedLandId);

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    /**
     * Applies {@code GameData.extraManaOnLandSubtypeTapThisTurn} — a turn-scoped, symmetric
     * "whenever a player taps a land of this subtype for mana, that player adds an additional
     * {X}" granted by a resolved effect rather than a permanent's static ability (Chaos Moon's
     * odd branch). Like the static land-tap triggers above it is a triggered mana ability, so it
     * pays straight into the tapping player's pool without using the stack.
     */
    private void applyTurnScopedExtraLandTapMana(GameData gameData, UUID tappingPlayerId, UUID tappedLandId) {
        if (gameData.extraManaOnLandSubtypeTapThisTurn.isEmpty()) {
            return;
        }
        Permanent tappedLand = gameQueryService.findPermanentById(gameData, tappedLandId);
        if (tappedLand == null) {
            return;
        }
        Set<CardSubtype> types = gameQueryService.effectiveBasicLandTypes(gameData, tappedLand);
        ManaPool pool = gameData.playerManaPools.get(tappingPlayerId);
        if (pool == null) {
            return;
        }
        for (var entry : gameData.extraManaOnLandSubtypeTapThisTurn.entrySet()) {
            if (!types.contains(entry.getKey())) {
                continue;
            }
            for (var bonus : entry.getValue()) {
                ManaColor color = bonus.color() != ManaColor.COLORLESS
                        && gameData.playersWithColoredManaReplacementThisTurn.contains(bonus.controllerId())
                        ? ManaColor.WHITE : bonus.color();
                pool.add(color);
                gameLogService.append(gameData, GameLog.builder()
                        .text(gameData.playerIdToName.get(tappingPlayerId) + " adds 1 additional "
                                + color.getCode() + " mana.")
                        .build());
            }
        }
    }

    // ── Permanent-returned-to-hand triggers ────────────────────────────

    // ── Ally-permanent-sacrificed triggers ──────────────────────────────

    /**
     * Handles effects that trigger whenever one or more creatures controlled by a player deal
     * damage to a player. The dealers are supplied as one batch so the trigger fires once for the
     * event even when multiple creatures dealt damage simultaneously.
     */
    public void checkAllyCreaturesDealDamageToPlayerTriggers(GameData gameData, UUID sourceControllerId,
                                                              UUID damagedPlayerId,
                                                              List<Permanent> damageDealers) {
        if (sourceControllerId == null || damagedPlayerId == null
                || damageDealers == null || damageDealers.isEmpty()) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) {
            return;
        }

        var context = new TriggerContext.AllyCreaturesDealDamageToPlayer(
                damagedPlayerId, sourceControllerId, damageDealers);
        for (Permanent permanent : List.copyOf(battlefield)) {
            List<CardEffect> effects = new ArrayList<>(DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_PLAYER.effectsOf(permanent.getCard()));
            effects.addAll(permanent.getTemporaryTriggerEffects(DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_PLAYER.trigger()));
            effects.addAll(permanent.getPersistentTriggerEffects(DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_PLAYER.trigger()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, permanent, DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_PLAYER.trigger()));
            for (CardEffect effect : effects) {
                var match = new TriggerMatchContext(gameData, permanent, sourceControllerId, effect);
                dispatch(match, DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_PLAYER, effect, context);
            }
        }
    }

    /**
     * Handles effects that trigger whenever one or more creatures controlled by a player deal
     * damage to an opponent. The dealers are supplied as one batch so the trigger fires once for
     * the event even when multiple creatures dealt damage simultaneously.
     */
    public void checkAllyCreaturesDealDamageToOpponentTriggers(GameData gameData, UUID sourceControllerId,
                                                                UUID damagedPlayerId,
                                                                List<Permanent> damageDealers) {
        checkAllyCreaturesDealDamageToOpponentTriggers(gameData, sourceControllerId, damagedPlayerId,
                damageDealers, snapshotAllyCreaturesDealDamageToOpponentTriggers(gameData)
                        .getOrDefault(sourceControllerId, List.of()));
    }

    /** Watcher and its abilities at the time the damage event occurred. */
    public record AllyDamageTriggerSnapshot(Permanent permanent, List<CardEffect> effects) {
        public AllyDamageTriggerSnapshot {
            permanent = new Permanent(permanent);
            effects = List.copyOf(effects);
        }
    }

    /** Captures watchers before damage casualties remove them or the effects granting their abilities. */
    public Map<UUID, List<AllyDamageTriggerSnapshot>> snapshotAllyCreaturesDealDamageToOpponentTriggers(
            GameData gameData) {
        Map<UUID, List<AllyDamageTriggerSnapshot>> snapshots = new LinkedHashMap<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            List<CardEffect> effects = new ArrayList<>();
            if (!gameQueryService.hasLostPrintedAbilities(gameData, permanent)) {
                effects.addAll(DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_OPPONENT.effectsOf(permanent.getCard()));
            }
            effects.addAll(permanent.getTemporaryTriggerEffects(DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_OPPONENT.trigger()));
            effects.addAll(permanent.getPersistentTriggerEffects(DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_OPPONENT.trigger()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, permanent, DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_OPPONENT.trigger()));
            if (!effects.isEmpty()) {
                snapshots.computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                        .add(new AllyDamageTriggerSnapshot(permanent, effects));
            }
        });
        return snapshots;
    }

    public void checkAllyCreaturesDealDamageToOpponentTriggers(GameData gameData, UUID sourceControllerId,
                                                               UUID damagedPlayerId, List<Permanent> damageDealers,
                                                               List<AllyDamageTriggerSnapshot> watchers) {
        if (sourceControllerId == null || damagedPlayerId == null
                || sourceControllerId.equals(damagedPlayerId)
                || damageDealers == null || damageDealers.isEmpty()) {
            return;
        }

        var context = new TriggerContext.AllyCreaturesDealDamageToPlayer(
                damagedPlayerId, sourceControllerId, damageDealers);
        for (AllyDamageTriggerSnapshot watcher : watchers) {
            for (CardEffect effect : watcher.effects()) {
                var match = new TriggerMatchContext(gameData, watcher.permanent(), sourceControllerId, effect);
                dispatch(match, DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_OPPONENT, effect, context);
            }
        }
    }

    /**
     * Records that {@code sacrificingPlayerId} sacrificed {@code sacrificedCard}, for "this turn" counts and sacrifice
     * boons. The abilities that trigger on the sacrifice ("whenever you sacrifice a permanent") are the trigger
     * pipeline's: the departure's rules event carries the sacrifice.
     */
    public void recordSacrifice(GameData gameData, UUID sacrificingPlayerId, Card sacrificedCard) {
        gameData.playersWhoSacrificedPermanentsThisTurn.add(sacrificingPlayerId);
        gameData.recordSacrificedPermanent(sacrificingPlayerId, sacrificedCard);
        checkSacrificeBoonTriggers(gameData, sacrificingPlayerId);
        if (gameData.simultaneousDyingPermanents.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
    }

    /** Fires finite digital sacrifice boons once for each successful permanent sacrifice. */
    private void checkSacrificeBoonTriggers(GameData gameData, UUID sacrificingPlayerId) {
        if (sacrificingPlayerId == null) {
            return;
        }
        if (gameData.simultaneousSacrificeDepth > 0
                && !gameData.sacrificeBoonBatchPlayerIds.add(sacrificingPlayerId)) {
            return;
        }
        List<SacrificeBoonWatcher> watchers = List.copyOf(gameData.sacrificeBoonWatchers);
        for (SacrificeBoonWatcher watcher : watchers) {
            if (!watcher.controllerId().equals(sacrificingPlayerId)
                    || watcher.remainingUses() <= 0
                    || !gameData.sacrificeBoonWatchers.remove(watcher)) {
                continue;
            }

            int remainingUses = watcher.remainingUses() - 1;
            if (remainingUses > 0) {
                gameData.sacrificeBoonWatchers.add(new SacrificeBoonWatcher(
                        watcher.controllerId(), watcher.sourceCard(), watcher.effect(), remainingUses));
            }

            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect()))));
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} sacrifice boon triggers ({} use(s) remain)", gameData.id,
                    watcher.sourceCard().getName(), remainingUses);
        }
    }

    /**
     * Puts one ability of {@code candidate} that watches another object's zone change on the stack for
     * {@code events}, binding the event data its effects need the way the old slot collector of {@code watch} did.
     * The trigger pipeline has already decided that it triggers: its relation, its filter, the zone it functions
     * from and the look-back of the event (CR 603.10a). {@code events} holds one event, or each event of the batch
     * that met a "one or more" ability's trigger condition, the first of which its effects bind.
     *
     * @return whether anything was put on the stack or queued for it
     */
    /**
     * The source permanent an old collector body puts on the stack. A permanent that left in the same event is its
     * last-known copy, whose card is a layered copy; the body names the card the candidate names instead, as the
     * pipeline's own stack entries do, so effects that act on "this card" find it.
     */
    private static Permanent namedSourcePermanent(TriggerCandidate candidate) {
        Permanent perm = candidate.sourceAsPermanent();
        if (candidate.sourcePermanent() == null || perm.getCard() == candidate.sourceCard()) {
            return perm;
        }
        Permanent named = new Permanent(perm);
        named.setCard(candidate.sourceCard());
        return named;
    }

    public boolean triggerZoneChange(GameData gameData, ZoneChangeWatch watch, TriggerCandidate candidate,
                                     List<CardEffect> effects, List<ZoneChangeEvent> events) {
        if (watch.entering()) {
            return triggerEntry(gameData, watch, candidate, effects, events);
        }
        ZoneChangeEvent event = events.getFirst();
        Permanent perm = namedSourcePermanent(candidate);
        UUID controllerId = candidate.controllerId();
        Card movedCard = lastKnownCard(event);
        boolean markOnAcceptance = candidate.ability().options().limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        boolean triggered = false;
        switch (watch) {
            case ON_ALLY_PERMANENT_SACRIFICED -> triggered = triggerAllyPermanentSacrificed(gameData, perm,
                    controllerId, effects, markOnAcceptance, event);
            case ON_OPPONENT_PERMANENT_SACRIFICED -> {
                var ctx = new TriggerContext.OpponentPermanentSacrificed(event.controllerId(), movedCard);
                for (CardEffect effect : effects) {
                    triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect), watch,
                            effect, ctx);
                }
            }
            case ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED -> {
                var ctx = new TriggerContext.OpponentNontokenPermanentSacrificed(event.controllerId(), movedCard);
                for (CardEffect effect : effects) {
                    triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect), watch,
                            effect, ctx);
                }
            }
            case ON_ANY_PERMANENT_SACRIFICED -> {
                var ctx = new TriggerContext.PermanentSacrificed(event.controllerId(), movedCard);
                for (CardEffect effect : effects) {
                    triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect), watch,
                            effect, ctx);
                }
            }
            case ON_ANY_CREATURE_SACRIFICED -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.AllySacrificed(event.controllerId(), movedCard, event.permanent()));
            case ON_ANY_NONCREATURE_ARTIFACT_SACRIFICED_OR_DESTROYED -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.NoncreatureArtifactSacrificedOrDestroyed(event.controllerId(), movedCard));
            case EXILE_ON_CONTROLLER_TOKEN_SACRIFICED -> {
                var ctx = new TriggerContext.AllySacrificed(event.controllerId(), movedCard, event.permanent());
                for (CardEffect effect : effects) {
                    triggered |= registry.dispatch(new TriggerMatchContext(gameData, null, controllerId, effect,
                            candidate.sourceCard()), watch, effect, ctx);
                }
            }
            case GRAVEYARD_ON_CONTROLLER_PERMANENT_SACRIFICED -> triggered = triggerGraveyardPermanentSacrificed(
                    gameData, candidate.sourceCard(), controllerId, effects);
            case ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_ANY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(
                    gameData, candidate, watch, effects, anyPermanentGraveyard(event));
            case GRAVEYARD_ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD,
                 GRAVEYARD_ON_CREATURE_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD -> {
                for (CardEffect effect : effects) {
                    triggered |= queueGraveyardCreaturePutTrigger(gameData, candidate.sourceCard(), controllerId,
                            effect);
                }
            }
            case ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_ARTIFACT_PUT_INTO_OPPONENT_OWNER_GRAVEYARD_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(
                    gameData, candidate, watch, effects, new TriggerContext.ArtifactGraveyard(event.ownerId(),
                            event.controllerId(), event.card(), movedCard.getManaValue(),
                            Map.copyOf(event.permanent().getCounters()), event.cause() == ZoneChangeCause.SACRIFICE));
            case ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                 ON_ALLY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.EnchantmentGraveyard(event.ownerId(), event.controllerId()));
            case ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(
                    gameData, candidate, watch, effects, new TriggerContext.OpponentPermanentGraveyard(event.card(),
                            event.controllerId(), event.ownerId(), new Permanent(event.permanent())));
            case ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> triggered =
                    dispatchAnyPermanentDeathTriggersForWatcher(gameData, controllerId, perm, effects,
                            watchesPermanentsYouControl(candidate), event.card(), anyPermanentGraveyard(event));
            case ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> {
                var ctx = anyPermanentGraveyard(event);
                for (CardEffect effect : effects) {
                    triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect), watch,
                            effect, ctx);
                }
            }
            case ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> {
                var ctx = anyPermanentGraveyard(event);
                for (CardEffect effect : effects) {
                    triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect), watch,
                            effect, ctx);
                }
            }
            case ON_PERMANENT_PUT_INTO_YOUR_GRAVEYARD_FROM_BATTLEFIELD -> {
                var ctx = anyPermanentGraveyard(event);
                for (CardEffect effect : effects) {
                    triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect), watch,
                            effect, ctx);
                }
            }
            case ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT -> triggered = dispatchWatchEffects(gameData, candidate,
                    watch, effects, new TriggerContext.LandPutIntoGraveyard(event.card(), event.ownerId(),
                            event.causeControllerId()));
            case ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects, new TriggerContext.NoncreaturePermanentDestroyed(movedCard,
                            event.controllerId(), event.causeControllerId()));
            case ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.AnyLandGraveyard(movedCard, event.ownerId(), event.controllerId()));
            case ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD -> {
                CreatureDeath death = CreatureDeath.of(event);
                Permanent enchanted = event.permanent();
                var ctx = new TriggerContext.EnchantedPermanentDeath(enchanted.getId(), event.controllerId(),
                        movedCard.getId(), death.power(), death.toughness(), movedCard.getManaValue(),
                        movedCard.hasType(CardType.CREATURE),
                        enchanted.cardsLeavingBattlefield().stream().map(Card::getId).toList(), event.ownerId(),
                        movedCard);
                for (CardEffect effect : effects) {
                    triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect), watch,
                            effect, ctx);
                }
            }
            case ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(
                    gameData, candidate, watch, effects,
                    new TriggerContext.AllyAuraOrEquipmentGraveyard(movedCard, event.controllerId()));
            case ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD -> {
                var ctx = new TriggerContext.EnchantedPermanentLeaves(new Permanent(event.permanent()),
                        event.controllerId(), event.to());
                for (CardEffect effect : effects) {
                    triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect), watch,
                            effect, ctx);
                }
            }
            case ON_ANOTHER_PERMANENT_LEAVES_BATTLEFIELD -> triggered = triggerAnotherPermanentLeaves(gameData, perm,
                    controllerId, effects, event);
            case ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD -> triggered = triggerAnotherCreatureLeaves(gameData, perm,
                    controllerId, effects, event);
            case ON_ANOTHER_ARTIFACT_LEAVES_BATTLEFIELD -> triggered = triggerAnotherArtifactLeaves(gameData, perm,
                    controllerId, effects, event);
            case ON_ANOTHER_NONTOKEN_ARTIFACT_PUT_INTO_GRAVEYARD_OR_EXILE_FROM_BATTLEFIELD -> triggered =
                    triggerAnotherNontokenArtifactPutIntoGraveyardOrExile(gameData, perm, controllerId, effects,
                            event);
            case ON_ALLY_CREATURE_LEAVES_BATTLEFIELD -> triggered = triggerAllyCreatureLeaves(gameData, perm,
                    controllerId, effects, event);
            case ON_ALLY_CREATURE_EXILED_FROM_BATTLEFIELD -> triggered = triggerAllyCreatureExiled(gameData, perm,
                    controllerId, effects, event);
            case ON_ANY_CREATURE_EXILED_FROM_BATTLEFIELD -> {
                var ctx = new TriggerContext.CreatureExiledFromBattlefield(new Permanent(event.permanent()),
                        event.controllerId(), Math.max(0, CreatureDeath.of(event).power()));
                // Only some effects see the creature's own exile.
                triggered = candidate.sourcePermanent() != null
                        && candidate.sourcePermanent().getId().equals(event.permanent().getId())
                        ? dispatchCounterReturnForLeavingSource(gameData, controllerId, perm, watch, effects, ctx)
                        : dispatchWatchEffects(gameData, candidate, watch, effects, ctx);
            }
            case ON_ANY_ARTIFACT_EXILED_FROM_BATTLEFIELD -> triggered = dispatchWatchEffects(gameData, candidate,
                    watch, effects, new TriggerContext.ArtifactExiledFromBattlefield(new Permanent(event.permanent()),
                            event.controllerId()));
            case ON_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING, ON_ALLY_CREATURE_LEAVES_BATTLEFIELD_WITHOUT_DYING,
                 ON_SELF_OR_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING -> {
                enqueueCreatureLeavesBattlefieldWithoutDyingTriggers(gameData, perm, controllerId, effects);
                triggered = !effects.isEmpty();
            }
            case ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD_DURING_CONTROLLER_TURN -> triggered =
                    triggerAllyPermanentLeavesDuringYourTurn(gameData, perm, controllerId, effects, event);
            case ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD -> triggered = triggerAllyPermanentLeaves(gameData, perm,
                    controllerId, effects, event);
            case ON_ALLY_PERMANENTS_LEAVE_BATTLEFIELD -> {
                enqueueAllyPermanentsLeaveBattlefieldTriggers(gameData, perm, controllerId, effects);
                triggered = !effects.isEmpty();
            }
            case ON_CONTROLLER_SPELL_OR_ABILITY_EXILES_PERMANENT -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects, new TriggerContext.ControllerSpellOrAbilityExilesPermanent(
                            new Permanent(event.permanent()), event.controllerId(), event.causeControllerId()));
            case ON_ANY_PERMANENT_RETURNED_TO_HAND -> triggered = triggerPermanentReturnedToHand(gameData, perm,
                    controllerId, effects, event);
            case ON_CONTROLLER_CREATURE_RETURNED_TO_HAND -> triggered = triggerControllerCreatureReturnedToHand(
                    gameData, perm, controllerId, effects, event);
            case ON_CONTROLLER_PERMANENT_RETURNED_TO_HAND -> triggered = triggerControllerPermanentReturnedToHand(
                    gameData, perm, controllerId, effects, event);
            case ON_CONTROLLER_ANOTHER_NONLAND_PERMANENT_RETURNED_TO_HAND -> triggered =
                    triggerControllerAnotherNonlandPermanentReturnedToHand(gameData, perm, controllerId, effects,
                            event);
            case ON_ANY_CARD_EXILED -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.CardExiled());
            case ON_CONTROLLER_CARDS_EXILED_DURING_TURN -> triggered = dispatchWatchEffects(gameData, candidate, watch,
                    effects, new TriggerContext.CardsExiledDuringTurn(controllerId));
            case ON_YOUR_COMMANDER_PUT_INTO_COMMAND_ZONE -> {
                if (isCommanderOf(gameData, event.card(), controllerId)) {
                    triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                            new TriggerContext.CommanderPutIntoCommandZone(event.card(), controllerId,
                                    event.permanent() == null ? Map.of()
                                            : snapshotCountersOnPermanent(event.permanent())));
                }
            }
            case ON_CONTROLLER_CARDS_EXILED_FROM_HAND -> triggered = dispatchWatchEffects(gameData, candidate, watch,
                    effects, new TriggerContext.ControllerCardsExiledFromHand(controllerId, events.size()));
            case ON_CONTROLLER_CARD_PUT_INTO_HAND_FROM_LIBRARY -> triggered = dispatchWatchEffects(gameData, candidate,
                    watch, effects, new TriggerContext.ControllerCardPutIntoHandFromLibrary(controllerId, event.card()));
            case ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE -> {
                // "The first time one or more land cards are put into your graveyard each turn": only if this event's
                // lands are all the land cards that entered your graveyard this turn.
                boolean firstLandEvent = gameData.landCardGraveyardEntriesThisTurn.getOrDefault(controllerId, 0)
                        <= events.size();
                List<CardEffect> eligible = effects.stream()
                        .filter(effect -> firstLandEvent || !(effect instanceof OncePerTurnTriggerEffect once
                                && once.firstLandGraveyardEvent()))
                        .toList();
                triggered = dispatchWatchEffects(gameData, candidate, watch, eligible,
                        new TriggerContext.LandPutIntoGraveyard(event.card(), controllerId, null));
            }
            case ON_ALLY_LAND_CARD_MILLED -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.LandCardMilled(event.card(), controllerId));
            case ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY,
                 ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.PermanentCardPutIntoGraveyard(event.card(), controllerId));
            case ON_ALLY_CREATURE_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.CreatureCardsPutIntoGraveyardFromLibrary(controllerId, events.size()));
            case ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects, new TriggerContext.CardsPutIntoGraveyardFromLibrary(controllerId,
                            events.size(), events.stream().map(ZoneChangeEvent::card).toList()));
            case ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.CreatureCardPutIntoGraveyardFromLibrary(event.card(), event.ownerId()));
            case ON_ANY_CARDS_PUT_INTO_LIBRARY -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.CardsPutIntoLibrary(event.ownerId(), events.size()));
            case ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE -> triggered = dispatchWatchEffects(gameData, candidate,
                    watch, effects, new TriggerContext.CardPutIntoGraveyard(event.card(), controllerId, event.from()));
            case ON_ALLY_ARTIFACT_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD, ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE ->
                    triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                            new TriggerContext.CardPutIntoGraveyard(event.card(), event.ownerId()));
            case ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
                 ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD,
                 ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.CreatureCardPutIntoGraveyard(event.card(), event.ownerId(), event.from()));
            case ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE -> {
                var ctx = new TriggerContext.CardPutIntoGraveyard(event.card(), event.ownerId());
                if (candidate.zone() == Zone.EXILE) {
                    // From exile only while suspended with time counters on it (Nihilith).
                    UUID exiledId = candidate.sourceCard().getId();
                    Integer timeCounters = gameData.exiledCardTimeCounters.get(exiledId);
                    if (!gameData.exiledCardsWithNonSuspendTimeCounters.contains(exiledId)
                            && timeCounters != null && timeCounters > 0) {
                        for (CardEffect effect : effects) {
                            triggered |= registry.dispatch(new TriggerMatchContext(gameData, null, controllerId,
                                    effect, candidate.sourceCard()), watch, effect, ctx);
                        }
                    }
                } else {
                    triggered = dispatchWatchEffects(gameData, candidate, watch, effects, ctx);
                }
            }
            case ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.BlackCardOpponentGraveyard(event.ownerId(), event.card()));
            case GRAVEYARD_ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE -> triggered = triggerGraveyardLandPutIntoGraveyard(
                    gameData, candidate.sourceCard(), controllerId, effects);
            case ON_OPPONENT_MILLS -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.Mill(event.ownerId(), events.size()));
            case ON_NONLAND_CARDS_MILLED -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.NonlandCardsMilled(event.ownerId(), events.size()));
            case GRAVEYARD_ON_OPPONENT_NONLAND_CARD_MILLED -> {
                Card card = candidate.sourceCard();
                for (CardEffect effect : effects) {
                    StackEntry milledTrigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                            card.getName() + "'s ability", new ArrayList<>(List.of(effect)));
                    milledTrigger.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                    gameData.enqueueTrigger(milledTrigger);
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    triggered = true;
                }
            }
            case ON_OPPONENT_CREATURE_CARD_MILLED -> {
                var ctx = new TriggerContext.CreatureCardMilled(event.ownerId(), event.card());
                for (CardEffect effect : effects) {
                    triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect), watch, effect,
                            ctx);
                }
            }
            case GRAVEYARD_ON_CREATURE_CARD_LEAVES_OPPONENT_GRAVEYARD, GRAVEYARD_ON_SELF_RETURNED_TO_HAND -> {
                Card card = candidate.sourceCard();
                for (CardEffect effect : effects) {
                    gameData.enqueueTrigger(new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                            card.getName() + "'s ability", new ArrayList<>(List.of(effect))));
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    triggered = true;
                }
            }
            case ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD, ON_CONTROLLER_ARTIFACT_OR_CREATURE_CARDS_LEAVE_GRAVEYARD ->
                    triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                            new TriggerContext.ControllerCardsLeaveGraveyard(controllerId,
                                    events.stream().map(ZoneChangeEvent::card).toList()));
            case ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD, ON_CONTROLLER_CREATURE_CARD_LEAVES_GRAVEYARD,
                 ON_CONTROLLER_ARTIFACT_CARDS_LEAVE_GRAVEYARD -> triggered = dispatchWatchEffects(gameData, candidate,
                    watch, effects, new TriggerContext.ControllerCardsLeaveGraveyard(controllerId));
            case ON_CONTROLLER_CARD_RETURNED_FROM_GRAVEYARD_TO_HAND -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.ControllerCardReturnedFromGraveyardToHand(controllerId, event.card()));
            case ON_CONTROLLER_INSTANT_OR_SORCERY_CARD_LEAVES_GRAVEYARD -> {
                Card card = event.card();
                if (!card.isToken() && (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))) {
                    triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                            new TriggerContext.ControllerInstantOrSorceryCardLeavesGraveyard(controllerId, card));
                }
            }
            case ON_CONTROLLER_CARDS_EXILED_FROM_GRAVEYARD -> triggered = dispatchWatchEffects(gameData, candidate,
                    watch, effects, new TriggerContext.ControllerCardsExiledFromGraveyard(controllerId, events.size()));
            case ON_CONTROLLER_CARDS_EXILED_FROM_LIBRARY_OR_GRAVEYARD -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects,
                    new TriggerContext.ControllerCardsExiledFromLibraryOrGraveyard(controllerId, events.size()));
            case ON_CONTROLLER_CREATURES_OR_CREATURE_CARDS_EXILED -> triggered = dispatchWatchEffects(gameData,
                    candidate, watch, effects, new TriggerContext.ControllerCreaturesOrCreatureCardsExiled(controllerId,
                            events.size(), events.stream()
                            .filter(moved -> !moved.card().isToken() && moved.card().hasType(CardType.CREATURE))
                            .map(ZoneChangeEvent::card).toList()));
            case ON_OPPONENT_OWNED_CARD_EXILED -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.OpponentOwnedCardExiled(event.ownerId(), event.card()));
            case ON_CARDS_EXILED_FROM_GRAVEYARDS_OR_BATTLEFIELD_DURING_YOUR_TURN -> triggered = dispatchWatchEffects(
                    gameData, candidate, watch, effects,
                    new TriggerContext.CardsExiledFromGraveyardsOrBattlefield(events.size()));
        }
        return triggered;
    }

    /** A permanent put into a graveyard from the battlefield, as the permanent-graveyard collectors read it. */
    private static TriggerContext.AnyPermanentGraveyard anyPermanentGraveyard(ZoneChangeEvent event) {
        CreatureDeath death = CreatureDeath.of(event);
        return new TriggerContext.AnyPermanentGraveyard(event.card(), event.controllerId(), event.ownerId(),
                new Permanent(event.permanent()), death.power(), death.toughness());
    }

    /** "Whenever another permanent leaves the battlefield". */
    private boolean triggerAnotherPermanentLeaves(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        UUID leavingId = leavingPermanent.getId();
        UUID ownerId = controllerId;
        for (CardEffect effect : effects) {
            if (effect instanceof TransformSelfWhenChosenPermanentLeavesEffect
                    && !leavingId.equals(perm.getChosenPermanentId())) continue;
            CardEffect resolved = effect;
            if (resolved instanceof LeavingPermanentIdAwareEffect aware) {
                resolved = aware.boundToLeavingPermanentId(leavingId);
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    ownerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(resolved)),
                    null,
                    perm.getId()
            );
            if (resolved instanceof TransformSelfWhenChosenPermanentLeavesEffect) {
                entry.setSourcePermanentSnapshot(new Permanent(perm));
            }
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another permanent leaving the battlefield ({})",
                    gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever another creature leaves the battlefield". */
    private boolean triggerAnotherCreatureLeaves(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        UUID leavingId = leavingPermanent.getId();
        UUID ownerId = controllerId;
        UUID leavingControllerId = event.controllerId();
        for (CardEffect effect : effects) {
            CardEffect resolved = effect;
            if (effect instanceof LeavingCreatureNameAwareEffect aware) {
                if (leavingPermanent.getCard().isToken()) continue;
                resolved = aware.boundToLeavingCreatureName(leavingPermanent.getCard().getName());
            }
            if (!perm.getCard().getTargetGroups().isEmpty()
                    && resolved.targetSpec().declaredTarget() != null) {
                // The card declares the trigger's target (Extractor Demon's "target player"), which is
                // chosen as the trigger is put on the stack.
                boolean playerTargetOnly = !resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT);
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        perm.getCard(), ownerId, new ArrayList<>(List.of(resolved)), playerTargetOnly,
                        perm.getCard().getTargetFilter(), 0, perm.getId(), new Permanent(perm), false,
                        leavingId, null, ownerId, null));
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another creature leaving the battlefield ({})",
                        gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    ownerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(resolved)),
                    null,
                    perm.getId()
            );
            entry.setTriggeringPermanentId(leavingId);
            entry.setTriggeringPermanentControllerId(leavingControllerId);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another creature leaving the battlefield ({})",
                    gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever another permanent you control leaves the battlefield". */
    private boolean triggerAllyPermanentLeaves(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        UUID leavingId = leavingPermanent.getId();
        for (CardEffect resolved : effects) {
            CardEffect dispatchEffect = resolved;
            if (dispatchEffect instanceof LeavingPermanentCountersAwareEffect aware) {
                dispatchEffect = aware.boundToLeavingPermanentCounters(snapshotCountersOnPermanent(leavingPermanent));
                if (dispatchEffect == null) continue;
            }
            if (dispatchEffect instanceof ConditionalEffect conditional
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forStaticEffect(perm, controllerId))) {
                continue;
            }
            if (dispatchEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || dispatchEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        perm.getCard(), controllerId, new ArrayList<>(List.of(dispatchEffect)), false,
                        perm.getCard().getTargetFilter(), 0, perm.getId()));
                gameLogService.append(gameData, GameLog.text(
                        perm.getCard().getName() + "'s ability triggers — choose a target."));
                continue;
            }
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    controllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(dispatchEffect)),
                    null,
                    perm.getId()
            ));
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another permanent you control leaving the battlefield ({})",
                    gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever another creature you control leaves the battlefield". */
    private boolean triggerAllyCreatureLeaves(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        UUID leavingId = leavingPermanent.getId();
        for (CardEffect resolved : effects) {
            CardEffect dispatchEffect = resolved;
            if (dispatchEffect instanceof LeavingCreatureCountersAwareEffect aware) {
                dispatchEffect = aware.boundToLeavingCreatureCounters(snapshotCountersOnPermanent(leavingPermanent));
            }
            if (dispatchEffect instanceof ConditionalEffect conditional
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forStaticEffect(perm, controllerId))) {
                continue;
            }
            if (dispatchEffect instanceof LeavingPermanentCountersAwareEffect aware) {
                dispatchEffect = aware.boundToLeavingPermanentCounters(snapshotCountersOnPermanent(leavingPermanent));
                if (dispatchEffect == null) continue;
            }
            if (dispatchEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || dispatchEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        perm.getCard(), controllerId, new ArrayList<>(List.of(dispatchEffect)), false,
                        perm.getCard().getTargetFilter(), 0, perm.getId()));
                gameLogService.append(gameData, GameLog.text(
                        perm.getCard().getName() + "'s ability triggers — choose a target."));
                continue;
            }
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    controllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(dispatchEffect)),
                    null,
                    perm.getId()
            ));
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another creature you control leaving the battlefield ({})",
                    gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever another permanent you control leaves the battlefield during your turn". */
    private boolean triggerAllyPermanentLeavesDuringYourTurn(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        UUID leavingId = leavingPermanent.getId();
        for (CardEffect resolved : effects) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    controllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(resolved)),
                    null,
                    perm.getId()
            ));
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another permanent you control leaving the battlefield ({})",
                    gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever another creature you control is put into exile from the battlefield". */
    private boolean triggerAllyCreatureExiled(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent exiledPermanent = event.permanent();
        for (CardEffect resolved : effects) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    controllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(resolved)),
                    null,
                    perm.getId()
            ));
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another creature you control being put into exile ({})",
                    gameData.id, perm.getCard().getName(), exiledPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever another artifact you control leaves the battlefield". */
    private boolean triggerAnotherArtifactLeaves(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        for (CardEffect effect : effects) {
            if (effect.targetSpec().declaredTarget() != null) {
                // The target is chosen as the trigger goes on the stack (CR 603.3d).
                boolean playerTargetOnly = !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT);
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        perm.getCard(), controllerId, new ArrayList<>(List.of(effect)), playerTargetOnly,
                        perm.getCard().getTargetFilter(), 0, perm.getId(), new Permanent(perm)));
            } else {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
            }
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another artifact leaving the battlefield ({})",
                    gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever another nontoken artifact you control is put into a graveyard or exiled from the battlefield". */
    private boolean triggerAnotherNontokenArtifactPutIntoGraveyardOrExile(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent leavingPermanent = event.permanent();
        Zone destination = event.to();
        for (CardEffect effect : effects) {
            if (effect.targetSpec().declaredTarget() != null) {
                boolean playerTargetOnly = !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT);
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        perm.getCard(), controllerId, new ArrayList<>(List.of(effect)), playerTargetOnly,
                        perm.getCard().getTargetFilter(), 0, perm.getId(), new Permanent(perm)));
            } else {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
            }
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another nontoken artifact entering {} from the battlefield ({})",
                    gameData.id, perm.getCard().getName(), destination, leavingPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever a permanent is returned to a player's hand": one stack object for its effects. */
    private boolean triggerPermanentReturnedToHand(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent returnedPermanent = event.permanent();
        UUID returnedToPlayerId = event.ownerId();
        List<CardEffect> resolvedEffects = new ArrayList<>(effects);
        if (resolvedEffects.isEmpty()) return false;

        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                perm.getCard(),
                controllerId,
                perm.getCard().getName() + "'s ability",
                resolvedEffects,
                returnedToPlayerId,
                perm.getId());
        // "That player" is the owner the permanent returned to — determined by the event, not chosen.
        trigger.setNonTargeting(true);
        gameData.stack.add(trigger);

        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} permanent-returned-to-hand trigger pushed onto stack",
                gameData.id, perm.getCard().getName());

        return true;
    }

    /** "Whenever a creature is returned to your hand from the battlefield". */
    private boolean triggerControllerCreatureReturnedToHand(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        Permanent returnedPermanent = event.permanent();
        UUID returnedToPlayerId = controllerId;
        for (CardEffect effect : effects) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    returnedToPlayerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    perm.getId()
            ));
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on a creature returned to its controller's hand ({})",
                    gameData.id, perm.getCard().getName(), returnedPermanent.getCard().getName());
        }
        return true;
    }

    /** "Whenever a permanent is returned to your hand". */
    private boolean triggerControllerPermanentReturnedToHand(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        UUID returnedToPlayerId = controllerId;
        for (CardEffect effect : effects) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    returnedToPlayerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    perm.getId()
            ));
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on a permanent returned to its controller's hand",
                    gameData.id, perm.getCard().getName());
        }
        return true;
    }

    /** "Whenever another nonland permanent you control is returned to its owner's hand". */
    private boolean triggerControllerAnotherNonlandPermanentReturnedToHand(GameData gameData, Permanent perm, UUID controllerId,
            List<CardEffect> effects, ZoneChangeEvent event) {
        UUID returnedPermanentControllerId = controllerId;
        for (CardEffect effect : effects) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    returnedPermanentControllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    perm.getId()
            ));
            gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on another nonland permanent returned to its owner's hand",
                    gameData.id, perm.getCard().getName());
        }
        return true;
    }

    /** Whether {@code card} is one of {@code ownerId}'s commanders, either face of it. */
    private static boolean isCommanderOf(GameData gameData, Card card, UUID ownerId) {
        return gameData.playerCommanders.getOrDefault(ownerId, List.of()).stream()
                .anyMatch(commander -> commander.getId().equals(card.getId())
                        || commander.getBackFaceCard() != null
                        && commander.getBackFaceCard().getId().equals(card.getId()));
    }

    /** "Whenever a land card is put into a graveyard from anywhere", from a graveyard. */
    private boolean triggerGraveyardLandPutIntoGraveyard(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                         List<CardEffect> effects) {
        boolean triggered = false;
        for (CardEffect resolved : effects) {
            if (resolved instanceof MayPayManaEffect mayPay) {
                gameData.queueMayAbility(sourceCard, sourceControllerId, mayPay, null);
            } else if (resolved instanceof MayEffect may) {
                gameData.queueMayAbility(sourceCard, sourceControllerId, may);
            } else {
                gameData.enqueueTrigger(new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard,
                        sourceControllerId, sourceCard.getName() + "'s ability", new ArrayList<>(List.of(resolved))));
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
            triggered = true;
        }
        return triggered;
    }

    /**
     * The permanent that entered in a zone change, as it is now (CR 603.6a: the game checks the permanents on the
     * battlefield after the event), or as it entered once it has left again.
     *
     * @param card         its card: the card it is now, or its entered copy's
     * @param permanent    the permanent on the battlefield, or {@code null} once it has left
     * @param controllerId the player it entered under the control of
     */
    private record Entering(Card card, Permanent permanent, UUID controllerId) {

        UUID permanentId() {
            return permanent == null ? null : permanent.getId();
        }
    }

    private Entering entering(GameData gameData, ZoneChangeEvent event) {
        Permanent live = gameQueryService.findPermanentById(gameData, event.permanent().getId());
        return new Entering(live != null ? live.getCard() : event.permanent().getCard(), live, event.controllerId());
    }

    /**
     * Puts {@code candidate}'s "whenever [an object] enters" ability, which the trigger pipeline matched to
     * {@code events}, on the stack the way {@code watch}'s old slot collector did. A "one or more" ability triggers
     * once, for the first of the batch's entries its effects accept, and the stack objects it queued name every
     * permanent of the batch that entered (CR 603.2c).
     *
     * @return whether it triggered
     */
    private boolean triggerEntry(GameData gameData, ZoneChangeWatch watch, TriggerCandidate candidate,
                                 List<CardEffect> effects, List<ZoneChangeEvent> events) {
        if (watch == ZoneChangeWatch.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD
                || watch == ZoneChangeWatch.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD) {
            return triggerTokensEnter(gameData, watch, candidate, effects, events);
        }
        boolean oneOrMore = candidate.ability().options().multiplicity() == TriggerMultiplicity.ONE_OR_MORE;
        if (!oneOrMore) {
            return triggerEntry(gameData, watch, candidate, effects, entering(gameData, events.getFirst()), false);
        }
        int stackSize = gameData.stack.size();
        for (ZoneChangeEvent event : events) {
            if (triggerEntry(gameData, watch, candidate, effects, entering(gameData, event), true)) {
                List<UUID> entered = events.stream().map(entry -> entry.permanent().getId()).toList();
                for (int index = stackSize; index < gameData.stack.size(); index++) {
                    StackEntry entry = gameData.stack.get(index);
                    entry.setTriggeringPermanentIds(entered);
                    if (watch == ZoneChangeWatch.ON_ALLY_LAND_ENTERS_BATTLEFIELD) {
                        for (ZoneChangeEvent other : events) {
                            entry.rememberLastKnownPermanentCard(other.permanent().getId(),
                                    other.permanent().getCard());
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean triggerEntry(GameData gameData, ZoneChangeWatch watch, TriggerCandidate candidate,
                                 List<CardEffect> effects, Entering entering, boolean oneOrMore) {
        Permanent perm = candidate.sourcePermanent();
        UUID controllerId = candidate.controllerId();
        Card sourceCard = candidate.sourceCard();
        TriggerLimit limit = candidate.ability().options().limit();
        return switch (watch) {
            case ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, ON_ALLY_CREATURES_ENTERS_BATTLEFIELD,
                 ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD, ON_SELF_OR_ALLY_CREATURES_ENTERS_BATTLEFIELD ->
                    triggerAllyCreatureEnters(gameData, watch, perm, controllerId, effects, entering, limit);
            case ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, ON_SELF_OR_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD ->
                    triggerAllyNontokenCreatureEnters(gameData, watch, perm, controllerId, effects, entering, limit);
            case COMMAND_ZONE_ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD -> {
                // Panharmonicon-style doublers only multiply triggers of permanents (CR 603.2d), so a command-zone
                // source is triggered exactly once.
                var ctx = new TriggerContext.PermanentEnters(entering.card(), controllerId, null, 1,
                        entering.permanentId());
                boolean triggered = false;
                for (CardEffect effect : effects) {
                    triggered |= dispatch(new TriggerMatchContext(gameData, null, controllerId, effect,
                            sourceCard), watch, effect, ctx);
                }
                yield triggered;
            }
            case ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD ->
                    triggerAllyArtifactEnters(gameData, watch, perm, controllerId, effects, entering, limit);
            case ON_SELF_OR_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD,
                 ON_SELF_OR_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD, ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD -> {
                var ctx = new TriggerContext.PermanentEnters(entering.card(), controllerId, null,
                        1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId,
                                entering.card()), entering.permanentId());
                boolean triggered = false;
                for (CardEffect effect : effects) {
                    triggered |= dispatchEntering(gameData, perm, controllerId, watch, effect, limit, ctx);
                }
                yield triggered;
            }
            case ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, ON_SELF_OR_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD ->
                    triggerAllyEnchantmentEnters(gameData, perm, controllerId, effects, entering, limit);
            case ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, ON_ANY_CREATURE_ENTERS_BATTLEFIELD ->
                    triggerAnyCreatureEnters(gameData, watch, perm, controllerId, effects, entering, limit);
            case ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD -> {
                var ctx = new TriggerContext.PermanentEnters(entering.card(), entering.controllerId(),
                        entering.controllerId(), 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId,
                        entering.controllerId(), entering.card()), null);
                boolean triggered = false;
                for (CardEffect effect : effects) {
                    triggered |= dispatchEntering(gameData, perm, controllerId, watch, effect, limit, ctx);
                }
                yield triggered;
            }
            case ON_OPPONENT_LAND_ENTERS_BATTLEFIELD ->
                    triggerOpponentLandEnters(gameData, watch, perm, controllerId, effects, entering);
            case ON_ALLY_LAND_ENTERS_BATTLEFIELD ->
                    triggerAllyLandEnters(gameData, perm, controllerId, effects, entering, oneOrMore, limit,
                            narrowedLandfall(candidate));
            case GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD ->
                    triggerGraveyardLandEnters(gameData, sourceCard, controllerId, effects, entering);
            case ON_ANY_PERMANENT_ENTERS_BATTLEFIELD -> {
                // The player who put the permanent onto the battlefield is the caster of the spell it resolved
                // from, even when a replacement such as Gather Specimens makes it enter under another player's
                // control (Nature's Wrath ruling: it matters who puts it there, not who controls it).
                UUID puttingPlayerId = entering.permanent() != null
                        && entering.permanent().getCastControllerId() != null
                        ? entering.permanent().getCastControllerId() : entering.controllerId();
                var ctx = new TriggerContext.PermanentEnters(entering.card(), entering.controllerId(),
                        puttingPlayerId, 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId,
                        entering.controllerId(), entering.card()), entering.permanentId());
                boolean triggered = false;
                for (CardEffect effect : effects) {
                    triggered |= dispatchEntering(gameData, perm, controllerId, watch, effect, limit, ctx);
                }
                yield triggered;
            }
            case ON_ALLY_PERMANENT_ENTERS_TRANSFORMED -> dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.PermanentEnters(entering.card(), controllerId, null,
                            1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId,
                                    entering.card()), entering.permanentId()));
            case ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD -> {
                var ctx = new TriggerContext.PermanentEnters(entering.card(), controllerId, null,
                        1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId,
                                entering.card()), entering.permanentId());
                boolean triggered = false;
                for (CardEffect effect : effects) {
                    triggered |= dispatchEntering(gameData, perm, controllerId, watch, effect, limit, ctx);
                }
                yield triggered;
            }
            case GRAVEYARD_ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD -> entering.permanent() != null
                    && queueGraveyardPermanentEntersTriggers(gameData, sourceCard, controllerId, effects,
                    entering.permanent());
            case GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, GRAVEYARD_ON_ANY_CREATURE_ENTERS_BATTLEFIELD ->
                    triggerGraveyardCreatureEnters(gameData, sourceCard, controllerId, effects, entering,
                            watch == ZoneChangeWatch.GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD);
            case GRAVEYARD_ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, GRAVEYARD_ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD ->
                    triggerGraveyardPermanentTypeEnters(gameData, sourceCard, controllerId, effects,
                            watch == ZoneChangeWatch.GRAVEYARD_ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD);
            case GRAVEYARD_ON_CREATURE_ENTERS_FROM_GRAVEYARD_OR_CAST_FROM_GRAVEYARD ->
                    triggerGraveyardCreatureEntersFromGraveyard(gameData, sourceCard, controllerId, effects, entering);
            case ON_CREATURE_ENTERS_FROM_GRAVEYARD -> {
                Permanent enteringPermanent = entering.permanent();
                if (enteringPermanent == null || !controllerId.equals(enteringPermanent.getEnteredFromGraveyardOwnerId())
                        || gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
                    yield false;
                }
                int triggerCount = 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId,
                        entering.controllerId(), entering.card());
                boolean triggered = false;
                for (CardEffect effect : effects) {
                    triggered |= registry.dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect), watch,
                            effect, new TriggerContext.PermanentEnters(entering.card(), entering.controllerId(), null,
                                    triggerCount, null));
                }
                yield triggered;
            }
            case ON_PERMANENT_ENTERS_FROM_GRAVEYARD, ON_PERMANENT_ENTERS_FROM_EXILE ->
                    triggerPermanentEntersFromZone(gameData, watch, perm, controllerId, effects, entering);
            case ON_SELF_ENTERS_FROM_GRAVEYARD -> triggerSelfEntersFromGraveyard(gameData, sourceCard, effects, entering);
            case ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD, ON_ENCHANTED_PLAYER_LAND_ENTERS_BATTLEFIELD -> {
                if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
                    yield false;
                }
                int triggerCount = 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId,
                        entering.controllerId(), entering.card());
                for (int i = 0; i < triggerCount; i++) {
                    StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, perm.getCard(), controllerId,
                            perm.getCard().getName() + "'s ability", new ArrayList<>(effects),
                            entering.controllerId(), perm.getId());
                    entry.setNonTargeting(true);
                    gameData.stack.add(entry);
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                }
                yield true;
            }
            default -> throw new IllegalArgumentException(watch + " is no entering watch");
        };
    }

    /**
     * "Whenever another creature you control enters", "this creature or another [restricted] creature you control",
     * "a creature you control", and their "one or more" forms. Sigil Captain's "if that creature is 1/1" is still a
     * wrapper (effect-slot-refactor.md step 14, run 3b).
     */
    private boolean triggerAllyCreatureEnters(GameData gameData, ZoneChangeWatch watch, Permanent perm,
                                              UUID controllerId, List<CardEffect> effects, Entering entering,
                                              TriggerLimit limit) {
        Card enteringCreature = entering.card();
        Permanent enteringPermanent = entering.permanent();
        var ctx = new TriggerContext.PermanentEnters(enteringCreature, controllerId, null,
                1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCreature),
                entering.permanentId());
        boolean triggered = false;
        for (CardEffect effect : effects) {
            CardEffect resolved = unwrapEnterCreatureConditional(gameData, enteringCreature, perm, effect);
            if (resolved == null) continue;
            if (!passesEnterInterveningIf(gameData, perm, controllerId, enteringPermanent, resolved)) continue;
            triggered |= dispatchEntering(gameData, perm, controllerId, watch, effect, resolved, limit, ctx);
        }
        return triggered;
    }

    /** "Whenever another nontoken creature you control enters", "this or another". */
    private boolean triggerAllyNontokenCreatureEnters(GameData gameData, ZoneChangeWatch watch, Permanent perm,
                                                      UUID controllerId, List<CardEffect> effects,
                                                      Entering entering, TriggerLimit limit) {
        var ctx = new TriggerContext.PermanentEnters(entering.card(), controllerId, null,
                1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, entering.card()),
                entering.permanentId());
        boolean triggered = false;
        for (CardEffect effect : effects) {
            if (!passesEnterInterveningIf(gameData, perm, controllerId, entering.permanent(), effect)) continue;
            triggered |= dispatchEntering(gameData, perm, controllerId, watch, effect, limit, ctx);
        }
        return triggered;
    }

    /**
     * "Whenever another artifact you control enters". An intervening "if" that counts permanents
     * (Voldaren Bloodcaster) is checked as it triggers and left wrapped for resolution (CR 603.4).
     */
    private boolean triggerAllyArtifactEnters(GameData gameData, ZoneChangeWatch watch, Permanent perm,
                                              UUID controllerId, List<CardEffect> effects, Entering entering,
                                              TriggerLimit limit) {
        Card enteringCard = entering.card();
        var ctx = new TriggerContext.PermanentEnters(enteringCard, controllerId, null,
                1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCard),
                entering.permanentId());
        boolean triggered = false;
        for (CardEffect effect : effects) {
            if (effect instanceof ConditionalEffect conditional
                    && conditional.condition() instanceof ControlsPermanentCount
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forPermanent(perm, controllerId))) {
                continue;
            }
            triggered |= dispatchEntering(gameData, perm, controllerId, watch, effect, limit, ctx);
        }
        return triggered;
    }

    /** "Whenever an enchantment you control enters" (constellation), "this or another". */
    private boolean triggerAllyEnchantmentEnters(GameData gameData, Permanent perm, UUID controllerId,
                                                 List<CardEffect> effects, Entering entering, TriggerLimit limit) {
        if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
            return false;
        }
        Card enteringCard = entering.card();
        Permanent enteringPermanent = entering.permanent();
        boolean triggered = false;
        for (CardEffect effect : effects) {
            CardEffect resolved = effect;
            if (resolved instanceof ConditionalEffect conditional && conditional.interveningIf()
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forPermanent(perm, controllerId)
                            .withTriggeringCard(enteringCard)
                            .withTriggeringPermanentId(entering.permanentId()))) {
                continue;
            }
            if (resolved instanceof ConditionalEffect conditional && conditional.interveningIf()
                    && conditional.condition() instanceof WasCast) {
                resolved = conditional.wrapped();
            }
            CardEffect dispatchEffect = resolved;
            // An ability limited to once each turn is one trigger per event, so an extra trigger from a doubler
            // does not give it a second copy.
            int triggerCount = limit != TriggerLimit.NONE ? 1
                    : 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, enteringCard);
            triggered = true;
            if (dispatchEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || dispatchEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                for (int i = 0; i < triggerCount; i++) {
                    gameData.queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                            perm.getCard(), controllerId, new ArrayList<>(List.of(dispatchEffect)), perm.getId(),
                            null, perm.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                }
                continue;
            }
            for (int i = 0; i < triggerCount; i++) {
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, perm.getCard(), controllerId,
                        perm.getCard().getName() + "'s ability", new ArrayList<>(List.of(dispatchEffect)), null,
                        perm.getId());
                // Ajani's Chosen needs the entering enchantment itself at resolution ("if that enchantment is an
                // Aura, you may attach it to the token").
                entry.setTriggeringCardId(enteringCard.getId());
                if (enteringPermanent != null) {
                    entry.setTriggeringPermanentId(enteringPermanent.getId());
                }
                if (capturesTriggeringAuraManaValue(dispatchEffect)) {
                    entry.setEventValue(enteringCard.getManaValue());
                }
                entry.setMarkSourceOncePerTurnOnAcceptance(limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN);
                if (enteringPermanent != null) {
                    entry.setAttachedPermanentSnapshot(new Permanent(enteringPermanent));
                }
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            }
        }
        return triggered;
    }

    /** "Whenever another creature enters", "whenever a creature enters", whoever controls it. */
    private boolean triggerAnyCreatureEnters(GameData gameData, ZoneChangeWatch watch, Permanent perm,
                                             UUID controllerId, List<CardEffect> effects, Entering entering,
                                             TriggerLimit limit) {
        Card enteringCreature = entering.card();
        UUID enteringControllerId = entering.controllerId();
        int extraTriggers = gameQueryService.countETBExtraTriggers(gameData, controllerId, enteringControllerId,
                enteringCreature);
        var ctx = new TriggerContext.PermanentEnters(enteringCreature, enteringControllerId, null, 1 + extraTriggers,
                null);
        boolean triggered = false;
        for (CardEffect effect : effects) {
            triggered |= dispatchEntering(gameData, perm, controllerId, watch, effect, limit, ctx);
        }
        return triggered;
    }

    /** "Whenever a land an opponent controls enters". */
    private boolean triggerOpponentLandEnters(GameData gameData, ZoneChangeWatch watch, Permanent perm,
                                              UUID controllerId, List<CardEffect> effects, Entering entering) {
        Card enteringLand = entering.card();
        UUID landControllerId = entering.controllerId();
        var ctx = new TriggerContext.PermanentEnters(enteringLand, landControllerId, landControllerId,
                1 + gameQueryService.countETBExtraTriggers(gameData, controllerId, landControllerId, enteringLand),
                null);
        boolean triggered = false;
        for (CardEffect effect : effects) {
            // Archaeomancer's Map uses an intervening "if": check the land counts both when the ability would
            // trigger and again when it resolves.
            if (effect instanceof ConditionalEffect conditional
                    && conditional.interveningIf()
                    && conditional.condition() instanceof com.github.laxika.magicalvibes.model.condition.TargetPlayerControlsMoreLandsThanController
                    && !gameQueryService.controlsMoreLandsThan(gameData, landControllerId, controllerId)) {
                continue;
            }
            CardEffect resolved = unwrapImprintedCardNameConditional(gameData, enteringLand, perm, effect);
            if (resolved == null) continue;
            resolved = unwrapPermanentEnteredThisTurnConditional(gameData, landControllerId, resolved);
            if (resolved == null) continue;
            triggered |= dispatchEnter(gameData, perm, controllerId, watch, resolved, ctx);
        }
        return triggered;
    }

    /**
     * Landfall: "whenever a land you control enters", and "one or more lands". A printed land doesn't see its own
     * entry unless its trigger narrows the lands down ({@code narrowed}: Valakut, the Molten Pinnacle's "a
     * Mountain"); a nonland permanent that is a land only through a continuous effect (Scute Swarm under Ashaya)
     * does.
     */
    private boolean triggerAllyLandEnters(GameData gameData, Permanent perm, UUID landControllerId,
                                          List<CardEffect> effects, Entering entering, boolean oneOrMore,
                                          TriggerLimit limit, boolean narrowed) {
        Card enteringLand = entering.card();
        UUID enteringPermanentId = entering.permanentId();
        if (perm.getId().equals(enteringPermanentId) && enteringLand.hasType(CardType.LAND) && !narrowed) {
            return false;
        }
        if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, landControllerId)) {
            return false;
        }
        int triggerCount = 1 + gameQueryService.countETBExtraTriggers(gameData, landControllerId, landControllerId,
                enteringLand);
        List<CardEffect> resolvedEffects = new ArrayList<>();
        for (CardEffect effect : effects) {
            CardEffect resolved = branchOnEnteringLand(gameData, effect, enteringPermanentId, perm);
            if (resolved == null) continue;
            if (resolved instanceof ConditionalEffect conditional
                    && conditional.interveningIf()
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forPermanent(perm, landControllerId)
                            .withTriggeringCard(enteringLand)
                            .withTriggeringPermanentId(enteringPermanentId))) {
                continue;
            }
            if (resolved instanceof ChooseOneAtTriggerTimeEffect atTriggerTime
                    && atTriggerTime.maximumChoices() == null) {
                resolved = atTriggerTime.choice();
            }
            resolvedEffects.add(resolved);
        }
        if (resolvedEffects.isEmpty()) {
            return false;
        }

        int permanentTriggerCount = triggerCount
                + gameQueryService.countAdditionalTriggeredAbilityTriggers(gameData, landControllerId, perm);
        if (resolvedEffects.size() == 1
                && resolvedEffects.getFirst() instanceof TriggeredModalEffect triggeredModalEffect) {
            for (int i = 0; i < permanentTriggerCount; i++) {
                gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                        perm.getCard(), landControllerId, triggeredModalEffect.choice(), perm.getId(),
                        false, false, null, null, enteringPermanentId));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            }
        } else if (resolvedEffects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))) {
            for (int i = 0; i < permanentTriggerCount; i++) {
                gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        perm.getCard(), landControllerId, resolvedEffects, null, 0, 0, 0, null, false,
                        enteringPermanentId));
                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                        "'s landfall ability triggers — choose a graveyard target."));
            }
        } else if (resolvedEffects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))) {
            boolean needsPlayerTarget = resolvedEffects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            boolean needsPermanentTarget = resolvedEffects.stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
            int targetGroupIndex = resolvedEffects.stream()
                    .mapToInt(perm.getCard()::getEffectTargetIndex)
                    .filter(index -> index >= 0 && index < perm.getCard().getTargetGroups().size())
                    .findFirst()
                    .orElse(-1);
            TargetFilter targetFilter = targetGroupIndex >= 0
                    ? perm.getCard().getTargetGroups().get(targetGroupIndex).getFilter()
                    : perm.getCard().getTargetFilter();
            boolean optionalTarget = targetGroupIndex >= 0
                    && perm.getCard().getTargetGroups().get(targetGroupIndex).getMinTargets() == 0
                    && perm.getCard().getTargetGroups().get(targetGroupIndex).getMaxTargets() == 1;
            for (int i = 0; i < permanentTriggerCount; i++) {
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        perm.getCard(), landControllerId, resolvedEffects, needsPlayerTarget && !needsPermanentTarget,
                        targetFilter, 0, perm.getId(), null, optionalTarget, enteringPermanentId, null,
                        landControllerId));
                gameLogService.append(gameData, GameLog.cardThen(perm.getCard(),
                        "'s landfall ability triggers — choose a target."));
            }
        } else if (resolvedEffects.size() == 1 && resolvedEffects.getFirst() instanceof ChooseOneEffect chooseOne) {
            for (int i = 0; i < permanentTriggerCount; i++) {
                gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                        perm.getCard(), landControllerId, chooseOne, perm.getId(),
                        false, false, null, null, enteringPermanentId));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            }
        } else {
            for (int i = 0; i < permanentTriggerCount; i++) {
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, perm.getCard(), landControllerId,
                        perm.getCard().getName() + "'s ability", new ArrayList<>(resolvedEffects), null,
                        perm.getId());
                entry.setTriggeringCardId(enteringLand.getId());
                entry.setTriggeringPermanentId(enteringPermanentId);
                entry.setTriggeringPermanentControllerId(landControllerId);
                entry.setMarkSourceOncePerTurnOnAcceptance(limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN);
                if (oneOrMore && enteringPermanentId != null) {
                    entry.setTriggeringPermanentIds(List.of(enteringPermanentId));
                    entry.rememberLastKnownPermanentCard(enteringPermanentId, enteringLand);
                }
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            }
        }
        return true;
    }

    /**
     * One branch of a landfall ability whose effects depend on the land that entered, "if it's a Forest, … otherwise
     * …" (Oran-Rief Hydra, Emeria Shepherd): the wrapped effect when the land matches, {@code null} when it doesn't,
     * or the effect unchanged when it isn't such a branch.
     */
    private CardEffect branchOnEnteringLand(GameData gameData, CardEffect effect, UUID enteringPermanentId,
                                            Permanent watcher) {
        if (!(effect instanceof TriggeringPermanentConditionalEffect conditional)) {
            return effect;
        }
        Permanent entering = enteringPermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, enteringPermanentId);
        return entering != null && predicateEvaluationService.matchesPermanentPredicate(
                entering, conditional.predicate(), FilterContext.of(gameData)
                        .withSourcePermanentId(watcher.getId()))
                ? conditional.wrapped() : null;
    }

    /** Landfall from the graveyard (Reach of Branches). */
    private boolean triggerGraveyardLandEnters(GameData gameData, Card card, UUID landControllerId,
                                               List<CardEffect> effects, Entering entering) {
        boolean triggered = false;
        for (CardEffect resolved : effects) {
            if (resolved instanceof ConditionalEffect conditional
                    && conditional.interveningIf()
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forCard(card, landControllerId).withTriggeringCard(entering.card()))) {
                continue;
            }
            if (resolved instanceof MayEffect may) {
                gameData.queueMayAbility(card, landControllerId, may);
                StackEntry landfallEntry = gameData.stack.getLast();
                landfallEntry.setTriggeringCardId(card.getId());
                landfallEntry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
            } else {
                StackEntry landfallEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, landControllerId,
                        card.getName() + "'s ability", new ArrayList<>(List.of(resolved)));
                landfallEntry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                gameData.stack.add(landfallEntry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
            triggered = true;
        }
        return triggered;
    }

    /**
     * From the graveyard, "whenever a creature you control enters" (Unconventional Tactics) and "whenever a creature
     * enters". The ability refers to this stay in the graveyard (CR 400.7).
     */
    private boolean triggerGraveyardCreatureEnters(GameData gameData, Card card, UUID controllerId,
                                                   List<CardEffect> effects, Entering entering, boolean stampVersion) {
        Permanent enteringPermanent = entering.permanent();
        boolean triggered = false;
        for (CardEffect resolved : effects) {
            if (resolved instanceof MayEffect may) {
                if (may.usesEnteringPermanentReference() && enteringPermanent != null) {
                    gameData.queueMayAbility(card, controllerId, may, enteringPermanent.getId(), null);
                } else {
                    gameData.queueMayAbility(card, controllerId, may);
                }
                if (stampVersion) {
                    gameData.stack.getLast().setTriggeringCardGraveyardEntryVersion(
                            gameData.graveyardEntryVersion(card.getId()));
                }
            } else {
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                        card.getName() + "'s ability", new ArrayList<>(List.of(resolved)));
                if (stampVersion) {
                    entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                }
                if (resolved.usesEnteringPermanentReference()) {
                    entry.setTargetId(enteringPermanent != null ? enteringPermanent.getId() : null);
                    entry.setNonTargeting(true);
                }
                gameData.enqueueTrigger(entry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
            triggered = true;
        }
        return triggered;
    }

    /** From the graveyard, "whenever an artifact you control enters" and "an enchantment you control". */
    private boolean triggerGraveyardPermanentTypeEnters(GameData gameData, Card card, UUID controllerId,
                                                        List<CardEffect> effects, boolean artifact) {
        boolean triggered = false;
        for (CardEffect resolved : effects) {
            if (resolved instanceof MayEffect may) {
                gameData.queueMayAbility(card, controllerId, may);
                if (artifact) {
                    StackEntry artifactEntry = gameData.stack.getLast();
                    artifactEntry.setTriggeringCardId(card.getId());
                    artifactEntry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                }
            } else {
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                        card.getName() + "'s ability", new ArrayList<>(List.of(resolved)));
                // The trigger refers to this stay in the graveyard (CR 400.7): a later re-entry makes the card a new
                // object, so the stale trigger's return does nothing.
                if (!artifact) {
                    entry.setTriggeringCardId(card.getId());
                }
                entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                gameData.stack.add(entry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
            triggered = true;
        }
        return triggered;
    }

    /** From the graveyard, "whenever a creature enters from your graveyard or you cast a creature from it". */
    private boolean triggerGraveyardCreatureEntersFromGraveyard(GameData gameData, Card card, UUID graveyardOwnerId,
                                                                List<CardEffect> effects, Entering entering) {
        Permanent enteringPermanent = entering.permanent();
        if (enteringPermanent == null) {
            return false;
        }
        boolean castFromGraveyard = enteringPermanent.isCast() && enteringPermanent.getCastFromZone() == Zone.GRAVEYARD;
        if (!graveyardOwnerId.equals(enteringPermanent.getEnteredFromGraveyardOwnerId())
                && !(castFromGraveyard && graveyardOwnerId.equals(entering.controllerId()))) {
            return false;
        }
        for (CardEffect effect : effects) {
            gameData.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, graveyardOwnerId,
                    card.getName() + "'s ability", new ArrayList<>(List.of(effect))));
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
        }
        return !effects.isEmpty();
    }

    /**
     * "Whenever this or another permanent enters from a graveyard" (River Kelpie) and "whenever a permanent you
     * control enters from exile": a non-targeting stack entry for each.
     */
    private boolean triggerPermanentEntersFromZone(GameData gameData, ZoneChangeWatch watch, Permanent perm,
                                                   UUID controllerId, List<CardEffect> effects, Entering entering) {
        Permanent enteringPermanent = entering.permanent();
        boolean fromExile = watch == ZoneChangeWatch.ON_PERMANENT_ENTERS_FROM_EXILE;
        if (enteringPermanent == null
                || (fromExile ? !enteringPermanent.isEnteredFromExile()
                : enteringPermanent.getEnteredFromGraveyardOwnerId() == null)
                || gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
            return false;
        }
        int triggerCount = 1 + gameQueryService.countETBExtraTriggers(gameData, controllerId,
                entering.controllerId(), entering.card());
        for (CardEffect effect : effects) {
            for (int i = 0; i < triggerCount; i++) {
                gameData.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, perm.getCard(), controllerId,
                        perm.getCard().getName() + "'s ability", new ArrayList<>(List.of(effect))));
                gameLogService.append(gameData, GameLog.cardTextCard(perm.getCard(), "'s ability triggers (",
                        entering.card(), fromExile ? " entered from exile)." : " entered from a graveyard)."));
            }
        }
        return !effects.isEmpty();
    }

    /**
     * "When this creature enters from a graveyard" (Treacherous Pit-Dweller). A targeting effect picks its target as
     * the ability goes on the stack (CR 603.3d): the permanent was never cast, so no target was chosen then.
     */
    private boolean triggerSelfEntersFromGraveyard(GameData gameData, Card enteringCard, List<CardEffect> effects,
                                                   Entering entering) {
        Permanent enteringPermanent = entering.permanent();
        UUID enteringControllerId = entering.controllerId();
        if (enteringPermanent == null || enteringPermanent.getEnteredFromGraveyardOwnerId() == null
                || gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, enteringControllerId)) {
            return false;
        }
        int triggerCount = 1 + gameQueryService.countETBExtraTriggers(gameData, enteringControllerId,
                enteringControllerId, enteringCard);
        boolean triggered = false;
        for (CardEffect effect : effects) {
            if (effect instanceof ConditionalEffect conditional
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forPermanent(enteringPermanent, enteringControllerId))) {
                continue;
            }
            boolean targets = effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT);
            for (int i = 0; i < triggerCount; i++) {
                if (targets) {
                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                            enteringCard, enteringControllerId, new ArrayList<>(List.of(effect)),
                            enteringPermanent.getId(), enteringCard.getTargetFilter()));
                } else {
                    gameData.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, enteringCard,
                            enteringControllerId, enteringCard.getName() + "'s ability",
                            new ArrayList<>(List.of(effect)), null, enteringPermanent.getId()));
                }
            }
            gameLogService.append(gameData, GameLog.cardThen(enteringCard,
                    "'s ability triggers (it entered from a graveyard)."));
            triggered = true;
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
        }
        return triggered;
    }

    /**
     * "Whenever one or more tokens you control enter" (also "whenever you create one or more tokens") and "one or
     * more tokens an opponent controls": once for the batch's tokens, or once for each token for an ability that
     * triggers for each, or whose effect's resolution condition is judged per token.
     */
    private boolean triggerTokensEnter(GameData gameData, ZoneChangeWatch watch, TriggerCandidate candidate,
                                       List<CardEffect> effects, List<ZoneChangeEvent> events) {
        Permanent permanent = candidate.sourcePermanent();
        UUID controllerId = candidate.controllerId();
        UUID tokenControllerId = events.getFirst().controllerId();
        boolean opponent = watch == ZoneChangeWatch.ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD;
        // "Whenever you create one or more tokens" isn't caused by permanents entering, so what changes the abilities
        // permanents entering trigger (Elesh Norn, Mother of Machines) doesn't apply to it.
        boolean creation = candidate.ability().conditions().stream().anyMatch(condition ->
                condition.trigger() instanceof ZoneChangeTrigger trigger
                        && trigger.causes().contains(ZoneChangeCause.CREATE));
        if (!creation && gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
            return false;
        }
        List<UUID> permanentIds = events.stream().map(event -> event.permanent().getId()).toList();
        int count = permanentIds.size();
        int extraTriggers = creation ? 0 : opponent
                ? gameQueryService.countETBExtraTriggersForAnyPermanent(gameData, controllerId)
                : gameQueryService.countETBExtraTriggers(gameData, controllerId, tokenControllerId,
                events.getFirst().permanent().getCard());
        boolean perToken = candidate.ability().options().multiplicity() == TriggerMultiplicity.EACH;
        TriggerLimit limit = candidate.ability().options().limit();
        boolean triggeredAny = false;
        for (CardEffect effect : effects) {
            boolean firstTokenCreation = effect instanceof OncePerTurnTriggerEffect once && once.firstTokenCreation();
            if (firstTokenCreation && gameData.tokensCreatedThisTurn.getOrDefault(controllerId, 0) > count) {
                continue;
            }
            // Akim, the Soaring Wind's "the first time" is still a wrapper (effect-slot-refactor.md step 14, run 3b).
            boolean oncePerTurn = effect instanceof OncePerTurnTriggerEffect || limit == TriggerLimit.ONCE_EACH_TURN
                    || limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
            boolean markOnAcceptance = effect instanceof OncePerTurnTriggerEffect once && once.markOnAcceptance()
                    || limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
            boolean limitedTrigger = oncePerTurn && !markOnAcceptance && !opponent;
            CardEffect resolved = unwrapOncePerTurnTrigger(gameData, permanent, effect);
            if (resolved == null) continue;
            boolean perTokenOccurrences = perToken || resolved.hasAbilityResolutionCondition();
            int occurrences = perTokenOccurrences && !perToken ? count : 1;
            for (int occurrence = 0; occurrence < occurrences; occurrence++) {
                TriggerContext effectContext = perTokenOccurrences
                        ? new TriggerContext.TokensEnter(1, limitedTrigger ? 1 : 1 + extraTriggers,
                        tokenIdAt(permanentIds, occurrence))
                        : firstTokenCreation || limitedTrigger
                        ? new TriggerContext.TokensEnter(count, 1, permanentIds)
                        : new TriggerContext.TokensEnter(count, 1 + extraTriggers, permanentIds);
                TriggerMatchContext match = new TriggerMatchContext(gameData, permanent, controllerId, resolved,
                        permanent.getCard(), markOnAcceptance);
                boolean triggered = registry.dispatch(match, watch, resolved, effectContext);
                triggeredAny |= triggered;
                if (triggered && oncePerTurn && !markOnAcceptance) {
                    if (effect instanceof OncePerTurnTriggerEffect) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(permanent.getId());
                    }
                    break;
                }
            }
        }
        return triggeredAny;
    }

    /**
     * Puts {@code candidate}'s ability, which the trigger pipeline matched to {@code events} (permanents phasing,
     * transforming or being turned face up), on the stack the way {@code watch}'s old slot collector did, with
     * {@code effects} its effects (intervening "if"s already wrapped).
     *
     * @return whether it triggered
     */
    public boolean triggerPermanentChange(GameData gameData, ZoneChangeWatch watch, TriggerCandidate candidate,
                                          List<CardEffect> effects, List<PermanentChangeEvent> events) {
        PermanentChangeEvent event = events.getFirst();
        Permanent perm = candidate.sourceAsPermanent();
        UUID controllerId = candidate.controllerId();
        Permanent changed = event.change() == PermanentChange.PHASED_OUT ? event.permanent()
                : Objects.requireNonNullElse(gameQueryService.findPermanentById(gameData, event.permanent().getId()),
                event.permanent());
        boolean triggered = false;
        switch (watch) {
            case ON_SELF_PHASES_OUT, ON_SELF_PHASES_IN -> triggered = enqueuePhasingTriggers(gameData, changed,
                    controllerId, effects, watch == ZoneChangeWatch.ON_SELF_PHASES_IN);
            case ON_ANY_OTHER_PERMANENT_PHASES_OUT -> triggered = dispatchWatchEffects(gameData, candidate, watch,
                    effects, new TriggerContext.PermanentPhasedOut(
                            events.stream().map(PermanentChangeEvent::permanent).toList()));
            case ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP, ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP,
                 ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP -> {
                var ctx = new TriggerContext.PermanentTurnsFaceUp(changed, event.controllerId());
                for (CardEffect effect : effects) {
                    triggered |= dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect), watch,
                            effect, ctx);
                }
            }
            case GRAVEYARD_ON_ALLY_PERMANENT_TURNS_FACE_UP -> {
                Card card = candidate.sourceCard();
                for (CardEffect effect : effects) {
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(card, controllerId, may);
                    } else {
                        gameData.enqueueTrigger(new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                                card.getName() + "'s ability", new ArrayList<>(List.of(effect))));
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    triggered = true;
                }
            }
            case ON_TURNED_FACE_UP -> triggered = turnFaceUpCopyService.putTurnedFaceUpAbilityOnStack(gameData,
                    changed, controllerId, effects, event.xValue());
            case ON_TRANSFORM_TO_BACK_FACE, ON_TRANSFORM_TO_FRONT_FACE -> triggered =
                    animationSupport.putTransformAbilityOnStack(gameData, changed, candidate.sourceCard(), effects);
            case ON_EQUIPPED_CREATURE_TRANSFORMS -> {
                Card equipmentCard = candidate.sourceCard();
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, equipmentCard, controllerId,
                        equipmentCard.getName() + "'s transform ability", new ArrayList<>(effects), null, perm.getId());
                entry.setSourcePermanentSnapshot(new Permanent(perm));
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(equipmentCard, "'s transform ability triggers."));
                triggered = true;
            }
            case ON_ALLY_PERMANENT_TRANSFORMS -> triggered = dispatchWatchEffects(gameData, candidate, watch, effects,
                    new TriggerContext.PermanentTransforms(changed, changed.getCard(), event.controllerId()));
            default -> throw new IllegalArgumentException(watch + " is no permanent-change watch");
        }
        return triggered;
    }

    /**
     * The card a zone change moved, as a "whenever [an object] …" ability reads it: for a departure, the card it
     * last existed on the battlefield as (CR 603.10a), with the characteristics it left with.
     */
    private static Card lastKnownCard(ZoneChangeEvent event) {
        return event.permanent() != null ? event.permanent().getCard() : event.card();
    }

    /**
     * Dispatches each effect of a watched ability as the old slot collectors' {@code dispatchSlot} did, with the
     * trigger condition read from the permanent's text-changed words for a printed ability (CR 612.1).
     */
    private boolean dispatchWatchEffects(GameData gameData, TriggerCandidate candidate, ZoneChangeWatch watch,
                                         List<CardEffect> effects, TriggerContext ctx) {
        Permanent perm = namedSourcePermanent(candidate);
        List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
        boolean markOnAcceptance = candidate.ability().options().limit() == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        boolean triggered = false;
        for (CardEffect effect : effects) {
            CardEffect changed = TextChangeTransformer.transform(effect, perm.getTextReplacements(),
                    globalWordChange);
            if (dispatchSlotEffect(gameData, perm, candidate.controllerId(), watch, ctx, changed,
                    markOnAcceptance)) {
                triggered = true;
            }
        }
        return triggered;
    }

    /**
     * "Whenever you sacrifice a [filter] permanent": each effect through its handler. {@code markOnAcceptance} says
     * whether taking the action uses up "do this only once each turn" (CR 603.2h).
     */
    private boolean triggerAllyPermanentSacrificed(GameData gameData, Permanent perm, UUID controllerId,
                                                   List<CardEffect> effects, boolean markOnAcceptance,
                                                   ZoneChangeEvent event) {
        var ctx = new TriggerContext.AllySacrificed(event.controllerId(), lastKnownCard(event), event.permanent());
        boolean triggered = false;
        for (CardEffect effect : effects) {
            var match = new TriggerMatchContext(gameData, perm, controllerId, effect, perm.getCard(),
                    markOnAcceptance);
            triggered |= dispatch(match, ZoneChangeWatch.ON_ALLY_PERMANENT_SACRIFICED, effect, ctx);
        }
        return triggered;
    }

    /** "When you sacrifice a [filter] permanent", from the graveyard. */
    private boolean triggerGraveyardPermanentSacrificed(GameData gameData, Card card, UUID controllerId,
                                                        List<CardEffect> effects) {
        boolean triggered = false;
        for (CardEffect resolved : effects) {
            if (resolved instanceof MayEffect may) {
                gameData.queueMayAbility(card, controllerId, may);
            } else {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(resolved))
                ));
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
            log.info("Game {} - {} graveyard permanent-sacrifice trigger queued", gameData.id, card.getName());
            triggered = true;
        }
        return triggered;
    }

    // ── Becomes-target-of-spell triggers ───────────────────────────────

    public void checkBecomesTargetOfSpellTriggers(GameData gameData) {
        if (gameData.stack.isEmpty()) return;
        StackEntry spellEntry = null;
        for (int i = gameData.stack.size() - 1; i >= 0; i--) {
            StackEntry candidate = gameData.stack.get(i);
            if (candidate.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                    && candidate.getEntryType() != StackEntryType.ACTIVATED_ABILITY) {
                spellEntry = candidate;
                break;
            }
        }
        if (spellEntry == null) return;
        chooseTargetAtRandomIfRequired(gameData, spellEntry);
        checkBecomesTargetOfSpellTriggers(gameData, spellEntry);
        checkTargetChoiceTriggers(gameData, spellEntry);
    }

    public void checkBecomesTargetOfSpellTriggers(GameData gameData, StackEntry spellEntry) {
        gameQueryService.withQueryScope(gameData, () -> {
            collectBecomesTargetOfSpellTriggers(gameData, spellEntry);
            return null;
        });

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class) && !gameData.interaction.isAwaitingInput()) {
            processNextSpellTargetTrigger(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            processNextETBTokenMultiTargetTrigger(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
        }
    }

    /**
     * Detects every becomes-the-target trigger of one spell's targeting event. All of them trigger
     * on the same event (CR 603.2) and only go on the stack afterwards (CR 603.3), so the caller runs
     * this under one query scope: the collectors' per-permanent layered queries share a single board
     * instead of each re-fingerprinting it, which would cost targets × collectors × battlefield size
     * board hashes. What the collectors write — triggered abilities on the stack, log lines, queued
     * choices and may abilities — is not read by the layered board or the static-bonus assembly.
     */
    private void collectBecomesTargetOfSpellTriggers(GameData gameData, StackEntry spellEntry) {
        if (spellEntry.getTargetZone() == Zone.STACK) {
            if (spellEntry.getTargetId() != null) {
                StackEntry targetedEntry = findStackEntryByCardId(gameData, spellEntry.getTargetId());
                collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, spellEntry);
            } else {
                for (UUID targetId : spellEntry.getTargetIds()) {
                    StackEntry targetedEntry = findStackEntryByCardId(gameData, targetId);
                    collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, spellEntry);
                }
            }
        }

        List<UUID> targetIds = new ArrayList<>();
        if (spellEntry.getTargetId() != null
                && spellEntry.getTargetZone() == null) {
            targetIds.add(spellEntry.getTargetId());
        }
        if (spellEntry.getTargetIds() != null) {
            targetIds.addAll(spellEntry.getTargetIds());
        }

        Set<UUID> targetedPlayers = new HashSet<>();
        Set<UUID> targetsWithAllyTrigger = new HashSet<>();
        Set<List<Object>> collectedAllyTargetTriggers = new HashSet<>();

        for (UUID targetId : new java.util.LinkedHashSet<>(targetIds)) {
            if (gameData.playerIds.contains(targetId)) {
                if (targetsWithAllyTrigger.add(targetId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
                            gameData, targetId, null, spellEntry, collectedAllyTargetTriggers);
                }
                if (targetedPlayers.add(targetId)) {
                    collectControllerBecomesTargetOfSpellTriggers(gameData, targetId, spellEntry);
                    collectControllerBecomesTargetOfOpponentTriggers(gameData, targetId, spellEntry);
                }
                continue;
            }

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());
            if (controllerId == null) continue;

            if (targetsWithAllyTrigger.add(targetId)) {
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
                                gameData, controllerId, targetPermanent, spellEntry,
                                collectedAllyTargetTriggers));
            }
            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                    () -> collectAllyPermanentBecomesTargetOfOpponentTriggers(
                            gameData, controllerId, targetPermanent, spellEntry));
            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                    () -> collectAnotherAllyPermanentBecomesTargetOfOpponentTriggers(
                            gameData, targetPermanent, controllerId, spellEntry));
            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                    () -> collectAllyCreatureBecomesTargetOfOpponentTriggers(
                            gameData, targetPermanent, controllerId, spellEntry));
            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                    () -> collectAllyCreatureBecomesTargetOfOpponentSpellTriggers(
                            gameData, targetPermanent, controllerId, spellEntry));
            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                    () -> collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
                            gameData, targetPermanent, controllerId, spellEntry));
            collectAnyCreatureBecomesTargetTriggers(gameData, targetPermanent, spellEntry);
            collectOpponentCreatureBecomesTargetOfYourSpellTriggers(gameData, targetPermanent, controllerId, spellEntry);
            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                    () -> collectAnotherAllyCreatureBecomesTargetOfSpellOrAbilityTriggers(
                            gameData, targetPermanent, controllerId));
            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                    () -> collectAllyCreatureBecomesTargetOfSpellTriggers(
                            gameData, targetPermanent, controllerId));
            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                    () -> collectAllyCreatureBecomesTargetOfInstantOrSorceryTriggers(
                            gameData, targetPermanent, controllerId, spellEntry));
            // Check the targeted permanent itself for "when this becomes the target" triggers.
            // Attached permanents (auras/equipment) use the loop below instead — their triggers
            // monitor the enchanted/equipped creature, not themselves (Spectral Prison is not
            // sacrificed when a spell targets the Aura rather than the enchanted creature).
            if (targetPermanent.isAttached() && (targetPermanent.getCard().getKeywords().contains(Keyword.WARD)
                    || hasGrantedCounterUnlessTrigger(gameData, targetPermanent))) {
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectBecomesTargetOfOpponentCounterTriggers(
                                gameData, targetPermanent, controllerId, spellEntry));
            }
            // A bestowed permanent (Crystalline Nautilus) is still "this permanent" while attached as an Aura, so its
            // own printed becomes-target abilities apply to it, unlike an ordinary Aura's (see the loop below).
            if (controllerId != null && (!targetPermanent.isAttached() || targetPermanent.isBestow())) {
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectBecomesTargetOfSpellOrAbilityTriggers(
                                gameData, targetPermanent, controllerId, spellEntry));
            }
            if (!targetPermanent.isAttached()) {
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectBecomesTargetTriggers(
                                gameData, targetPermanent, controllerId, targetPermanent, spellEntry));
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectBecomesTargetOfAuraSpellTriggers(
                                gameData, targetPermanent, controllerId, spellEntry));
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectBecomesTargetOfOpponentCounterTriggers(
                                gameData, targetPermanent, controllerId, spellEntry));
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectBecomesTargetOfOpponentSpellOnlyTriggers(
                                gameData, targetPermanent, controllerId, spellEntry.getControllerId()));
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
                                gameData, targetPermanent, controllerId, spellEntry.getControllerId()));
            }

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.isAttached()
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        // CR 603.3a: the triggered ability is controlled by the controller of the
                        // permanent that has it (the aura/equipment), not the enchanted creature —
                        // the two differ when an Aura like Spectral Prison enchants an opponent's creature.
                        // A bestowed permanent's printed becomes-target abilities refer to itself, so they do not
                        // fire when the creature it enchants is targeted; only its granted ability does (on that creature).
                        if (!attached.isBestow()) {
                            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                                    () -> collectBecomesTargetOfSpellOrAbilityTriggers(
                                            gameData, attached, playerId, spellEntry));
                        }
                        if (playerId.equals(controllerId)) {
                            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                                    () -> collectBecomesTargetTriggers(
                                            gameData, attached, playerId, targetPermanent, spellEntry));
                            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                                    () -> collectBecomesTargetOfAuraSpellTriggers(
                                            gameData, attached, playerId, spellEntry));
                            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                                    () -> collectBecomesTargetOfOpponentCounterTriggers(
                                            gameData, attached, playerId, spellEntry));
                        } else {
                            collectBecomesTargetTriggers(gameData, attached, playerId, targetPermanent, spellEntry);
                            collectBecomesTargetOfAuraSpellTriggers(gameData, attached, playerId, spellEntry);
                            collectBecomesTargetOfOpponentCounterTriggers(gameData, attached, playerId, spellEntry);
                        }
                    }
                }
            }
        }
    }

    private StackEntry findStackEntryByCardId(GameData gameData, UUID cardId) {
        return gameData.stack.stream()
                .filter(entry -> entry.getCard() != null && entry.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }

    /** Drains pending multi-target trigger choices (ETB token copies and ON_SELF_CAST multi-target). */
    public void processNextETBTokenMultiTargetTrigger(GameData gameData) {
        etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
    }

    public boolean needsSlotBySlotTargetSelection(Card card) {
        return etbTokenTargetService.needsSlotBySlotTargetSelection(card);
    }

    /**
     * Checks becomes-target triggers for activated/triggered abilities that target permanents.
     * Must be called after an ability is pushed onto the stack with a target permanent.
     */
    public void checkBecomesTargetOfAbilityTriggers(GameData gameData) {
        if (gameData.stack.isEmpty()) return;
        StackEntry abilityEntry = gameData.stack.getLast();
        chooseTargetAtRandomIfRequired(gameData, abilityEntry);
        checkBecomesTargetOfAbilityTriggers(gameData, abilityEntry);
        checkTargetChoiceTriggers(gameData, abilityEntry);
    }

    /**
     * Checks becomes-target triggers for an explicitly supplied activated or triggered ability.
     * This overload is used when the ability is not the current top stack entry, such as while
     * Psychic Battle is resolving.
     */
    public void checkBecomesTargetOfAbilityTriggers(GameData gameData, StackEntry abilityEntry) {
        if (abilityEntry == null) return;
        gameQueryService.withQueryScope(gameData, () -> {
            collectBecomesTargetOfAbilityTriggers(gameData, abilityEntry);
            return null;
        });

        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)
                && !gameData.interaction.isAwaitingInput()) {
            processNextSpellTargetTrigger(gameData);
        }
    }

    /**
     * The ability counterpart of {@link #collectBecomesTargetOfSpellTriggers}, run under one query
     * scope for the same reason.
     */
    private void collectBecomesTargetOfAbilityTriggers(GameData gameData, StackEntry abilityEntry) {
        if (abilityEntry.getTargetZone() == Zone.STACK && !abilityEntry.isNonTargeting()) {
            if (abilityEntry.getTargetId() != null) {
                StackEntry targetedEntry = findStackEntryByCardId(gameData, abilityEntry.getTargetId());
                collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, abilityEntry);
            } else {
                for (UUID targetId : abilityEntry.getTargetIds()) {
                    StackEntry targetedEntry = findStackEntryByCardId(gameData, targetId);
                    collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(gameData, targetedEntry, abilityEntry);
                }
            }
        }

        List<UUID> targetIds = new ArrayList<>();
        if (abilityEntry.getTargetId() != null
                && (abilityEntry.getTargetZone() == null || abilityEntry.getTargetZone() == Zone.BATTLEFIELD)
                && !abilityEntry.isNonTargeting()) {
            targetIds.add(abilityEntry.getTargetId());
        }
        if (abilityEntry.getTargetIds() != null) {
            targetIds.addAll(abilityEntry.getTargetIds());
        }

        if (!abilityEntry.isNonTargeting()) {
            for (UUID permanentId : abilityEntry.getMixedZoneTargetPermanentIds().values()) {
                if (!targetIds.contains(permanentId)) targetIds.add(permanentId);
            }
        }

        collectAllyCreatureBecomesTargetOfActivatedAbilityTriggers(gameData, abilityEntry, targetIds);

        Set<UUID> targetedPlayers = new HashSet<>();
        Set<UUID> targetsWithAllyTrigger = new HashSet<>();
        Set<List<Object>> collectedAllyTargetTriggers = new HashSet<>();

        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) {
                if (targetsWithAllyTrigger.add(targetId)) {
                    collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
                            gameData, targetId, null, abilityEntry, collectedAllyTargetTriggers);
                }
                if (targetedPlayers.add(targetId)) {
                    collectControllerBecomesTargetOfOpponentTriggers(gameData, targetId, abilityEntry);
                }
                continue;
            }

            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPermanent == null) continue;

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());

            if (controllerId != null) {
                if (targetsWithAllyTrigger.add(targetId)) {
                    collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                            () -> collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
                                    gameData, controllerId, targetPermanent, abilityEntry,
                                    collectedAllyTargetTriggers));
                }
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectAllyPermanentBecomesTargetOfOpponentTriggers(
                                gameData, controllerId, targetPermanent, abilityEntry));
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectAnotherAllyPermanentBecomesTargetOfOpponentTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry));
            }

            // Check the targeted permanent itself for "when this becomes the target" triggers.
            // Attached permanents (auras/equipment) use the loop below instead.
            if (targetPermanent.isAttached() && controllerId != null
                    && targetPermanent.getCard().getKeywords().contains(Keyword.WARD)) {
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectBecomesTargetOfOpponentCounterTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry));
            }
            if (controllerId != null && (!targetPermanent.isAttached() || targetPermanent.isBestow())) {
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectBecomesTargetOfSpellOrAbilityTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry));
            }
            if (!targetPermanent.isAttached() && controllerId != null) {
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectBecomesTargetOfOpponentCounterTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry));
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry.getControllerId()));
            }

            // Check for "whenever a creature you control becomes the target of opponent's spell or ability"
            if (controllerId != null) {
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectAllyCreatureBecomesTargetOfOpponentTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry));
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry));
                collectOpponentCreatureBecomesTargetOfYourSpellTriggers(gameData, targetPermanent, controllerId, abilityEntry);
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectAnotherAllyCreatureBecomesTargetOfSpellOrAbilityTriggers(
                                gameData, targetPermanent, controllerId));
                collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                        () -> collectAllyCreatureBecomesTargetOfBackupAbilityTriggers(
                                gameData, targetPermanent, controllerId, abilityEntry));
            }

            collectAnyCreatureBecomesTargetTriggers(gameData, targetPermanent, abilityEntry);

            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) continue;
                for (Permanent attached : battlefield) {
                    if (attached.isAttached()
                            && attached.getAttachedTo().equals(targetPermanent.getId())) {
                        // CR 603.3a: triggered ability controlled by the aura/equipment's controller. A bestowed
                        // permanent's printed abilities refer to itself, so they do not fire for the creature it enchants.
                        if (!attached.isBestow()) {
                            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                                    () -> collectBecomesTargetOfSpellOrAbilityTriggers(
                                            gameData, attached, playerId, abilityEntry));
                        }
                        if (playerId.equals(controllerId)) {
                            collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, controllerId,
                                    () -> collectBecomesTargetOfOpponentCounterTriggers(
                                            gameData, attached, playerId, abilityEntry));
                        } else {
                            collectBecomesTargetOfOpponentCounterTriggers(gameData, attached, playerId, abilityEntry);
                        }
                    }
                }
            }
        }
    }

    /**
     * Collects the batched trigger for "one or more creatures you control become the target of an
     * activated ability". A multi-target ability creates one trigger per watching permanent.
     */
    private void collectAllyCreatureBecomesTargetOfActivatedAbilityTriggers(
            GameData gameData, StackEntry abilityEntry, List<UUID> targetIds) {
        if (abilityEntry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                || abilityEntry.isCopy()) {
            return;
        }

        Set<UUID> targetedCreatureControllers = new HashSet<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)) continue;
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId != null) {
                targetedCreatureControllers.add(controllerId);
            }
        }

        for (UUID controllerId : targetedCreatureControllers) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;
            for (Permanent source : List.copyOf(battlefield)) {
                if (source.isCloaked() || source.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                for (CardEffect authoredEffect : TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_ACTIVATED_ABILITY.effectsOf(source.getCard())) {
                    CardEffect resolvedEffect = OncePerTurnTriggerSupport.unwrapIfAvailable(
                            gameData, source, authoredEffect);
                    if (resolvedEffect == null) continue;
                    gameData.enqueueTrigger(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source.getCard(),
                            controllerId,
                            source.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(resolvedEffect)),
                            null,
                            source.getId()));
                    OncePerTurnTriggerSupport.markIfNeeded(gameData, source, authoredEffect);
                    gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
                    log.info("Game {} - {} triggers when an ally creature becomes the target of an activated ability",
                            gameData.id, source.getCard().getName());
                }
            }
        }
    }

    /**
     * Checks all permanents controlled by the targeted player for effects that watch that player or
     * one of their permanents becoming the target of an opponent's spell or ability.
     */
    private void collectAllyPermanentOrPlayerBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID targetControllerId, Permanent targetPermanent,
            StackEntry triggeringEntry, Set<List<Object>> collectedTriggers) {
        if (targetControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            List<CardEffect> effects = new ArrayList<>(TargetWatch.ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.effectsOf(source.getCard()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source, TargetWatch.ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.trigger()));
            effects = effects.stream()
                    .map(effect -> resolveTriggeringPermanentConditional(
                            gameData, source, targetControllerId, targetPermanent, effect))
                    .filter(Objects::nonNull)
                    .toList();
            if (effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (!(effect instanceof CounterUnlessEffect counterEffect)) continue;

                StackEntry counterEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        targetControllerId,
                        source.getCard().getName() + "'s triggered ability",
                        new ArrayList<>(List.of(counterEffect)),
                        triggeringEntry.getTargetableId(),
                        Zone.STACK,
                        source.getId()
                );
                counterEntry.setSourcePermanentSnapshot(new Permanent(source));
                enqueueTargetTriggeredAbility(gameData, targetPermanent, targetControllerId, source, counterEntry);

                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
                log.info("Game {} - {} ally-permanent-or-player counter trigger queued",
                        gameData.id, source.getCard().getName());
            }

            // "You and/or at least one permanent you control becomes the target" is a single trigger for
            // the whole spell or ability, however many of those objects it targets. "You or a permanent
            // you control becomes the target" with a card draw (Rayne, Academy Chancellor: "triggers once
            // for each such target", 2004 ruling) triggers once for each target instead.
            effects = effects.stream()
                    .filter(effect -> !(effect instanceof CounterUnlessEffect))
                    .filter(effect -> isPerTargetDrawTrigger(effect)
                            || collectedTriggers.add(List.of(source.getId(), effect)))
                    .toList();
            if (effects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    targetControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    effects,
                    triggeringEntry.getControllerId(),
                    source.getId()
            );
            entry.setNonTargeting(true);
            enqueueTargetTriggeredAbility(gameData, targetPermanent, targetControllerId, source, entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} ally-permanent-or-player-becomes-target-of-opponent trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    private static boolean isPerTargetDrawTrigger(CardEffect effect) {
        CardEffect inner = effect;
        while (true) {
            if (inner instanceof ConditionalEffect conditional) {
                inner = conditional.wrapped();
            } else if (inner instanceof MayEffect may) {
                inner = may.wrapped();
            } else {
                return inner instanceof DrawCardEffect;
            }
        }
    }

    private void collectAnotherAllyPermanentBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID targetControllerId, StackEntry triggeringEntry) {
        if (targetControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            if (source.getId().equals(targetPermanent.getId())) continue;

            List<CardEffect> effects = new ArrayList<>(TargetWatch.ON_ANOTHER_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.effectsOf(source.getCard()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source, TargetWatch.ON_ANOTHER_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.trigger()));
            for (CardEffect effect : effects) {
                if (!(effect instanceof CounterUnlessEffect counterEffect)) continue;

                StackEntry counterEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        targetControllerId,
                        source.getCard().getName() + "'s triggered ability",
                        new ArrayList<>(List.of(counterEffect)),
                        triggeringEntry.getTargetableId(),
                        Zone.STACK,
                        source.getId()
                );
                counterEntry.setSourcePermanentSnapshot(new Permanent(source));
                enqueueTargetTriggeredAbility(gameData, targetPermanent, targetControllerId, source, counterEntry);

                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
                log.info("Game {} - {} another-ally-permanent counter trigger queued",
                        gameData.id, source.getCard().getName());
            }
            effects = effects.stream()
                    .filter(effect -> !(effect instanceof CounterUnlessEffect))
                    .toList();
            if (effects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    targetControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    effects,
                    null,
                    source.getId()
            );
            entry.setNonTargeting(true);
            entry.setTriggeringPermanentId(targetPermanent.getId());
            entry.setTriggeringPermanentControllerId(targetControllerId);
            enqueueTargetTriggeredAbility(gameData, targetPermanent, targetControllerId, source, entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} another-ally-permanent-becomes-target-of-opponent trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    private void collectAllyPermanentBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID targetControllerId, Permanent targetPermanent,
            StackEntry triggeringEntry) {
        if (targetControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            List<CardEffect> effects = new ArrayList<>(TargetWatch.ON_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.effectsOf(source.getCard()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source,
                    TargetWatch.ON_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.trigger()));
            if (effects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    targetControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    effects,
                    null,
                    source.getId()
            );
        entry.setSourcePermanentSnapshot(new Permanent(source));
        enqueueTargetTriggeredAbility(gameData, targetPermanent, targetControllerId, source, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        }
    }

    private void enqueueTargetTriggeredAbility(GameData gameData, Permanent targetCreature,
                                                UUID targetControllerId, Permanent source,
                                                StackEntry entry) {
        gameData.stack.add(entry);
    }

    private void collectWithAllyCreatureBecomesTargetCopies(GameData gameData, Permanent targetCreature,
                                                             UUID targetControllerId, Runnable collector) {
        int previousCopies = gameData.beginTriggeredAbilityCopies(1
                + gameQueryService.countAdditionalTriggeredAbilityTriggersForAllyCreatureBecomesTarget(
                gameData, targetControllerId, targetCreature, targetCreature));
        try {
            collector.run();
        } finally {
            gameData.restoreTriggeredAbilityCopies(previousCopies);
        }
    }

    /**
     * High Troller: as a spell or ability with a single target is put on the stack, its target is
     * chosen uniformly at random from its legal targets instead of by its controller. The chosen
     * target may be the one originally announced.
     */
    private void chooseTargetAtRandomIfRequired(GameData gameData, StackEntry entry) {
        if (!entry.isSingleTarget() || entry.isNonTargeting()) {
            return;
        }
        Permanent[] randomizer = new Permanent[1];
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (randomizer[0] == null
                    && !gameQueryService.hasLostAllAbilities(gameData, permanent)
                    && permanent.getCard().getStaticEffects().stream()
                    .anyMatch(ChooseTargetsAtRandomEffect.class::isInstance)) {
                randomizer[0] = permanent;
            }
        });
        if (randomizer[0] == null) {
            return;
        }
        UUID currentTarget = entry.getTargetId() != null
                ? entry.getTargetId()
                : entry.getTargetIds().isEmpty() ? null : entry.getTargetIds().getFirst();
        if (currentTarget == null) {
            return;
        }
        List<UUID> legalTargets = targetRedirectionSupport.collectValidTargetsIncludingCurrent(gameData, entry);
        if (legalTargets.isEmpty()) {
            return;
        }
        UUID selectedTarget = legalTargets.get(
                java.util.concurrent.ThreadLocalRandom.current().nextInt(legalTargets.size()));
        if (selectedTarget.equals(currentTarget)) {
            return;
        }
        entry.replaceTargetReferences(currentTarget, selectedTarget);
        gameLogService.append(gameData, GameLog.cardTextCard(
                randomizer[0].getCard(), " chooses the target of ", entry.getCard(), " at random."));
    }

    public void checkTargetChoiceTriggers(GameData gameData, StackEntry targetEntry) {
        if (targetEntry == null || !targetEntry.hasAnyTarget() || targetEntry.isNonTargeting()) {
            return;
        }

        gameData.forEachPermanent((controllerId, permanent) -> {
            for (CardEffect effect : TargetWatch.ON_ANY_PLAYER_CHOOSES_TARGETS.effectsOf(permanent.getCard())) {
                CardEffect effectToQueue = effect;
                if (effect instanceof TargetChoiceTriggerEffect targetChoiceEffect) {
                    if (gameQueryService.hasLostAllAbilities(gameData, permanent)
                            || (targetEntry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                            && targetEntry.getEntryType() != StackEntryType.TRIGGERED_ABILITY)
                            || !controllerId.equals(targetEntry.getControllerId())
                            || !targetsPlayerOrPermanent(gameData, targetEntry)) {
                        continue;
                    }
                    effectToQueue = OncePerTurnTriggerSupport.unwrapIfAvailable(
                            gameData, permanent, targetChoiceEffect.wrapped());
                    if (effectToQueue == null) {
                        continue;
                    }
                    OncePerTurnTriggerSupport.markIfNeeded(gameData, permanent, targetChoiceEffect.wrapped());
                }
                if (effect instanceof ReselectTargetAtRandomEffect
                        && (!targetEntry.isSingleTarget() || gameQueryService.hasLostAllAbilities(gameData, permanent))) {
                    continue;
                }
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        permanent.getCard(),
                        controllerId,
                        permanent.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effectToQueue)),
                        null,
                        permanent.getId()
                );
                trigger.setTriggeringCardId(targetEntry.getTargetableId());
                gameData.enqueueTrigger(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                log.info("Game {} - {} triggers when targets are chosen", gameData.id,
                        permanent.getCard().getName());
            }
        });
    }

    private boolean targetsPlayerOrPermanent(GameData gameData, StackEntry targetEntry) {
        if (targetEntry.getTargetZone() == null
                && isPlayerOrPermanentTarget(gameData, targetEntry.getTargetId())) {
            return true;
        }
        return targetEntry.getTargetIds().stream()
                .anyMatch(targetId -> isPlayerOrPermanentTarget(gameData, targetId));
    }

    private boolean isPlayerOrPermanentTarget(GameData gameData, UUID targetId) {
        return targetId != null
                && (gameData.playerIds.contains(targetId)
                || gameQueryService.findPermanentById(gameData, targetId) != null);
    }

    private void collectBecomesTargetTriggers(GameData gameData, Permanent source, UUID controllerId,
                                              Permanent targetedCreature, StackEntry spellEntry) {
        if (source.isCloaked() || gameQueryService.hasLostPrintedAbilities(gameData, source)) return;
        List<CardEffect> effects = new ArrayList<>(TargetWatch.ON_BECOMES_TARGET_OF_SPELL.effectsOf(source.getCard()));
        if (source == targetedCreature) {
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source, TargetWatch.ON_BECOMES_TARGET_OF_SPELL.trigger()));
        }
        if (effects.isEmpty()) return;

        // Split targeting effects (which choose "any target" at resolution — Livewire Lash's damage)
        // from non-targeting ones (which resolve against the source permanent — an Illusion's
        // "sacrifice it"). The latter must go straight on the stack carrying sourcePermanentId; the
        // any-target interaction never records a source, so a self-referential effect would no-op.
        List<CardEffect> targetingEffects = new ArrayList<>();
        List<CardEffect> nonTargetingEffects = new ArrayList<>();
        List<CardEffect> triggeringSpellEffects = new ArrayList<>();
        for (CardEffect effect : effects) {
            effect = resolveTriggeringSpellControllerConditional(effect, controllerId, spellEntry);
            if (effect == null) continue;
            if (effect instanceof TriggeringSpellReferencingEffect) {
                triggeringSpellEffects.add(effect);
            } else if (effect.targetSpec().declaredTarget() == null) {
                nonTargetingEffects.add(effect);
            } else {
                targetingEffects.add(effect);
            }
        }

        if (!nonTargetingEffects.isEmpty()) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(nonTargetingEffects),
                    null,
                    source.getId()
            );
            entry.setSourcePermanentSnapshot(new Permanent(source));
            enqueueTargetTriggeredAbility(gameData, targetedCreature, controllerId, source, entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} becomes-target-of-spell trigger queued", gameData.id, source.getCard().getName());
        }

        if (!triggeringSpellEffects.isEmpty()) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(triggeringSpellEffects),
                    null,
                    source.getId()
            );
            entry.setSourcePermanentSnapshot(new Permanent(source));
            entry.setTriggeringCardId(spellEntry.getCard().getId());
            entry.setTriggeringPermanentControllerId(spellEntry.getControllerId());
            enqueueTargetTriggeredAbility(gameData, targetedCreature, controllerId, source, entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} becomes-target-of-spell trigger queued against the targeting spell", gameData.id, source.getCard().getName());
        }

        if (!targetingEffects.isEmpty()) {
            // The targeted creature has the ability, so its controller chooses the target and it is the source.
            UUID abilityControllerId = source == targetedCreature
                    ? gameQueryService.findPermanentController(gameData, targetedCreature.getId())
                    : controllerId;
            if (abilityControllerId == null) abilityControllerId = controllerId;
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    targetedCreature.getCard(), abilityControllerId, new ArrayList<>(targetingEffects),
                    false, null, 0, source == targetedCreature ? targetedCreature.getId() : null,
                    source == targetedCreature ? new Permanent(targetedCreature) : null, false, null, null,
                    abilityControllerId, null, null, false
            ));

            gameLogService.append(gameData, GameLog.cardThen(targetedCreature.getCard(), "'s triggered ability triggers — choose a target for damage."));
            log.info("Game {} - {} becomes-target-of-spell trigger queued", gameData.id, targetedCreature.getCard().getName());
        }
    }

    private CardEffect resolveTriggeringSpellControllerConditional(CardEffect effect, UUID sourceControllerId,
                                                                    StackEntry spellEntry) {
        if (!(effect instanceof TriggeringSpellControllerConditionalEffect conditional)) {
            return effect;
        }
        if (!sourceControllerId.equals(spellEntry.getControllerId())) {
            return null;
        }
        return conditional.wrapped();
    }

    private void collectBecomesTargetOfSpellOrAbilityTriggers(
            GameData gameData, Permanent source, UUID controllerId, StackEntry triggeringEntry) {
        // Every targeting of a permanent counts toward "for the first time each turn", including targetings
        // that happened before a granting source entered the battlefield.
        boolean targetedEarlierThisTurn = source.isBecomeTargetCounterUsedThisTurn();
        source.setBecomeTargetCounterUsedThisTurn(true);
        // A face-down cloaked permanent has no printed abilities, but abilities granted to it still apply.
        List<CardEffect> effects = new ArrayList<>(source.isCloaked()
                || gameQueryService.hasLostPrintedAbilities(gameData, source)
                ? List.<CardEffect>of()
                : TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY.effectsOf(source.getCard()));
        effects.addAll(source.getTemporaryTriggerEffects(TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY.trigger()));
        effects.addAll(source.getPersistentTriggerEffects(TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY.trigger()));
        // Dismiss into Dream continuously grants "When this creature becomes the target of a spell
        // or ability, sacrifice it" to each creature its controller's opponents control.
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                gameData, source, TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY.trigger()));
        // Makeshift Mannequin: while a permanent has a mannequin counter, it has "When this creature
        // becomes the target of a spell or ability, sacrifice it." The grant comes from the resolving
        // spell (CR 611.2b), so it is tracked on the permanent rather than read from the counter alone.
        if (source.isMannequinGrantActive() && source.getCounterCount(CounterType.MANNEQUIN) > 0
                && !gameQueryService.hasLostPrintedAbilities(gameData, source)) {
            effects.add(new SacrificeSelfEffect());
        }
        if (effects.isEmpty()) return;

        boolean oncePerTurn = false;
        List<CardEffect> resolvedEffects = new ArrayList<>(effects.size());
        for (CardEffect effect : effects) {
            CardEffect resolved = effect;
            if (resolved instanceof TriggeringSpellControllerConditionalEffect conditional) {
                if (!controllerId.equals(triggeringEntry.getControllerId())) continue;
                resolved = conditional.wrapped();
            }
            if (resolved instanceof TriggeringSpellOpponentControllerConditionalEffect conditional) {
                if (controllerId.equals(triggeringEntry.getControllerId())) continue;
                resolved = conditional.wrapped();
            }
            if (resolved instanceof OncePerTurnTriggerEffect once) {
                if (gameData.oncePerTurnTriggersFiredThisTurn.contains(source.getId())) continue;
                resolved = once.wrapped();
                oncePerTurn = true;
            }
            resolvedEffects.add(resolved);
        }
        if (resolvedEffects.isEmpty()) return;

        // Glyph Keeper: "Whenever this creature becomes the target of a spell or ability for the first
        // time each turn, counter that spell or ability." A counterspelling effect in this slot fires at
        // most once per turn per permanent and counters the object that triggered it — set as the target
        // in the STACK zone so CounterSpellEffect finds the triggering entry sitting below this trigger.
        if (resolvedEffects.stream().anyMatch(CounterSpellingEffect.class::isInstance)) {
            // Only the plain "counter that spell or ability" form is limited to the first time each turn;
            // other counterspelling triggers (e.g. Dragon's Disciple's ward-like counter unless pays) are not.
            if (resolvedEffects.stream().anyMatch(CounterSpellEffect.class::isInstance)) {
                if (targetedEarlierThisTurn) return;
            }

            StackEntry counterEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(resolvedEffects),
                    0,
                    triggeringEntry.getTargetableId(),
                    source.getId(),
                    null,
                    Zone.STACK,
                    null,
                    null
            );
            enqueueTargetTriggeredAbility(gameData, source, controllerId, source, counterEntry);
            if (oncePerTurn) gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    "'s triggered ability triggers — counter that spell or ability."));
            log.info("Game {} - {} becomes-target-of-spell-or-ability counter trigger queued",
                    gameData.id, source.getCard().getName());
            return;
        }

        if (resolvedEffects.stream().anyMatch(TriggeringSpellReferencingEffect.class::isInstance)) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(resolvedEffects),
                    0,
                    triggeringEntry.getCard().getId(),
                    source.getId(),
                    null,
                    Zone.STACK,
                    null,
                    null
            );
            entry.setTriggeringCardId(triggeringEntry.getCard().getId());
            entry.setTriggeringPermanentControllerId(triggeringEntry.getControllerId());
            if (resolvedEffects.stream().anyMatch(
                    com.github.laxika.magicalvibes.model.effect.TriggeringObjectControllerGainsControlOfEnchantedPermanentEffect.class::isInstance)) {
                entry.setTriggeringPermanentId(source.getAttachedTo());
                entry.setNonTargeting(true);
            }
            if (resolvedEffects.stream().anyMatch(
                    com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringSpellControllerEffect.class::isInstance)) {
                // The triggering spell or ability is only referenced for its controller, so it leaving
                // the stack (countered, or the Herder ability resolved) does not fizzle the damage.
                entry.setNonTargeting(true);
            }
            if (resolvedEffects.stream().anyMatch(
                    com.github.laxika.magicalvibes.model.effect.TriggeringSpellControllerSacrificesPermanentsEffect.class::isInstance)) {
                // The spell or ability is only referenced, so it leaving the stack does not stop
                // its controller (remembered above) from sacrificing.
                entry.setNonTargeting(true);
            }
            enqueueTargetTriggeredAbility(gameData, source, controllerId, source, entry);
            if (oncePerTurn) gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} becomes-target-of-spell-or-ability trigger queued against the triggering object",
                    gameData.id, source.getCard().getName());
            return;
        }

        boolean targetsPermanent = resolvedEffects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        boolean targetsPlayer = resolvedEffects.stream()
                .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        if (targetsPermanent || targetsPlayer) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    source.getCard(), controllerId, new ArrayList<>(resolvedEffects),
                    targetsPlayer && !targetsPermanent, source.getCard().getTargetFilter(),
                    0, source.getId()));
            if (oncePerTurn) gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    "'s triggered ability triggers — choose a target."));
            log.info("Game {} - {} becomes-target-of-spell-or-ability trigger queued for target selection",
                    gameData.id, source.getCard().getName());
            return;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(resolvedEffects),
                null,
                source.getId()
        );
        if (source.isAttached()) {
            entry.setSourcePermanentSnapshot(new Permanent(source));
        }
        enqueueTargetTriggeredAbility(gameData, source, controllerId, source, entry);
        if (oncePerTurn) gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
        log.info("Game {} - {} becomes-target-of-spell-or-ability trigger queued", gameData.id, source.getCard().getName());
    }

    /**
     * "Whenever this creature becomes the target of an Aura spell, &lt;effect&gt;." Narrower than
     * {@link TargetWatch#ON_BECOMES_TARGET_OF_SPELL}: only Aura spells count, and there is no controller
     * restriction, so an opponent's Aura triggers it just as the controller's own does. Used by
     * Fugitive Druid.
     */
    private void collectBecomesTargetOfAuraSpellTriggers(GameData gameData, Permanent source, UUID controllerId, StackEntry spellEntry) {
        if (source.isCloaked()) return;
        Card spellCard = spellEntry.getTargetingCard();
        if (spellCard == null || !spellCard.isAura()) return;

        List<CardEffect> effects = TargetWatch.ON_BECOMES_TARGET_OF_AURA_SPELL.effectsOf(source.getCard());
        if (effects.isEmpty()) return;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                source.getId()
        );
        enqueueTargetTriggeredAbility(gameData, source, controllerId, source, entry);

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
        log.info("Game {} - {} becomes-target-of-Aura-spell trigger queued", gameData.id, source.getCard().getName());
    }

    private void collectBecomesTargetOfOpponentCounterTriggers(GameData gameData, Permanent source, UUID controllerId, StackEntry triggeringEntry) {
        // Only trigger if the spell/ability is controlled by an opponent
        if (controllerId.equals(triggeringEntry.getControllerId())) return;

        boolean wardSuppressed = gameQueryService.preventsOpponentCreatureWardTriggers(
                gameData, triggeringEntry.getControllerId())
                && hasWardAbility(gameData, source);
        if (source.isFaceDown()
                && (source.isCloaked() || source.isFaceDownWard())
                && gameQueryService.hasKeyword(gameData, source, Keyword.WARD)) {
            if (wardSuppressed) return;
            enqueueCloakedWardTrigger(gameData, source, controllerId, triggeringEntry);
        }

        List<CardEffect> effects = new ArrayList<>();
        boolean sourceIsTarget = source.getId().equals(triggeringEntry.getTargetId())
                || source.getCard().getId().equals(triggeringEntry.getTargetId())
                || triggeringEntry.getDeclaredTargetIds().contains(source.getId())
                || triggeringEntry.getDeclaredTargetIds().contains(source.getCard().getId())
                || triggeringEntry.getMixedZoneTargetPermanentIds().containsValue(source.getId());
        if (!source.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, source)
                && (sourceIsTarget || !source.getCard().getKeywords().contains(Keyword.WARD))) {
            effects.addAll(TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL.effectsOf(source.getCard()));
        }
        effects.addAll(source.getTemporaryTriggerEffects(TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL.trigger()));
        effects.addAll(source.getPersistentTriggerEffects(TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL.trigger()));
        // Granted ward ("each legendary permanent you control has ward {2}") is a "this permanent becomes
        // the target" trigger, so an attached permanent only gets it when it is itself the target.
        List<CardEffect> grantedEffects = grantedTriggeredAbilitySupport.grantedTriggerEffects(
                gameData, source, TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL.trigger()).stream()
                .filter(effect -> sourceIsTarget || !isCounterUnlessTrigger(effect))
                .toList();
        effects.addAll(grantedEffects);
        boolean printedWardRemoved = source.getCard().getKeywords().contains(Keyword.WARD)
                && !gameQueryService.hasKeyword(gameData, source, Keyword.WARD);
        if (printedWardRemoved) {
            // A printed ward whose keyword was removed (e.g. "permanents your opponents control lose ward")
            // no longer has its counter-unless trigger.
            effects.removeIf(this::isCounterUnlessTrigger);
        }
        if (wardSuppressed) {
            // Ward suppression only removes ward: a printed ward keyword's counter-unless goes, and so
            // does a granted ward, but a creature's own non-ward "counter unless" trigger stays (CR 702.21).
            boolean printedWard = gameQueryService.hasKeyword(gameData, source, Keyword.WARD);
            effects.removeIf(effect -> isCounterUnlessTrigger(effect)
                    && (printedWard || grantedEffects.contains(effect)));
        }
        boolean triggeringIsSpell = triggeringEntry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                && triggeringEntry.getEntryType() != StackEntryType.TRIGGERED_ABILITY;
        if (!triggeringIsSpell && !gameQueryService.hasKeyword(gameData, source, Keyword.WARD)) {
            // "Whenever this creature becomes the target of a spell an opponent controls" (Reality Smasher)
            // does not see abilities; only a Ward keyword's discard form also counters abilities.
            effects.removeIf(effect -> effect instanceof CounterUnlessDiscardsEffect
                    && !grantedEffects.contains(effect));
        }
        if (effects.isEmpty()) return;

        for (CardEffect effect : effects) {
            CardEffect resolvedEffect = effect;
            if (effect instanceof ConditionalEffect conditional) {
                if (!conditionEvaluationService.isInterveningIfMet(gameData, conditional, source, controllerId)) {
                    continue;
                }
                resolvedEffect = conditional.wrapped();
            }
            if (resolvedEffect instanceof CounterUnlessEffect counterEffect) {
                // Put the counter-unless effect directly on the stack targeting the spell. The pay
                // and discard variants queue an identical trigger entry; only the log wording differs
                // by the kind of ransom demanded.
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        controllerId,
                        source.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(counterEffect)),
                        triggeringEntry.getTargetableId(),
                        Zone.STACK,
                        source.getId()
                );
                entry.setSourcePermanentSnapshot(new Permanent(source));
                entry.setNonTargeting(true);
                enqueueTargetTriggeredAbility(gameData, source, controllerId, source, entry);

                switch (counterEffect.ransomKind()) {
                    case PAY_MANA -> {
                        String paymentText = counterEffect instanceof CounterUnlessPaysEffect pays
                                ? (pays.manaCost() != null ? pays.manaCost() : "{" + pays.amount() + "}")
                                        + (pays.lifeCost() > 0 ? " and " + pays.lifeCost() + " life" : "")
                                : "{" + counterEffect.ransomMagnitude() + "}";
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                                "'s triggered ability triggers — counter unless controller pays "
                                + paymentText + "."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell counter trigger queued", gameData.id, source.getCard().getName());
                    }
                    case PAY_WATERBEND -> {
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                                "'s triggered ability triggers — counter unless controller pays {"
                                        + counterEffect.ransomMagnitude() + "} using waterbend."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell waterbend counter trigger queued",
                                gameData.id, source.getCard().getName());
                    }
                    case DISCARD_CARD -> {
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers — counter unless controller discards a card."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell counter-unless-discard trigger queued", gameData.id, source.getCard().getName());
                    }
                    case DISCARD_CARD_OR_PAY_MANA -> {
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers — counter unless controller discards a card or pays mana."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell counter-unless-discard-or-pay trigger queued", gameData.id, source.getCard().getName());
                    }
                    case PAY_LIFE -> {
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                                "'s triggered ability triggers — counter unless controller pays life equal to its power."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell counter-unless-life trigger queued", gameData.id, source.getCard().getName());
                    }
                    case SACRIFICE_PERMANENT -> {
                        String sacrificeText = counterEffect instanceof CounterUnlessSacrificesEffect sacrifices
                                ? " " + sacrifices.requiredCount() + " " + sacrifices.sacrificeDescription()
                                : " a permanent";
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                                "'s triggered ability triggers - counter unless controller sacrifices"
                                        + sacrificeText + "."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell counter-unless-sacrifice trigger queued", gameData.id, source.getCard().getName());
                    }
                    case COLLECT_EVIDENCE -> {
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                                "'s triggered ability triggers - counter unless controller collects evidence."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell counter-unless-collect-evidence trigger queued",
                                gameData.id, source.getCard().getName());
                    }
                    case BLIGHT -> {
                        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                                "'s triggered ability triggers - counter unless controller blights a creature."));
                        log.info("Game {} - {} becomes-target-of-opponent-spell blight ward trigger queued",
                                gameData.id, source.getCard().getName());
                    }
                }
            }
        }
    }

    public void checkCollectEvidenceTriggers(GameData gameData, UUID collectingPlayerId) {
        var ctx = new TriggerContext.CollectEvidence(collectingPlayerId);
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(collectingPlayerId);
        if (ownBattlefield == null) {
            return;
        }

        for (Permanent perm : List.copyOf(ownBattlefield)) {
            dispatchWatch(gameData, perm, collectingPlayerId,
                    ActionWatch.ON_CONTROLLER_COLLECTS_EVIDENCE, ctx);
        }
    }

    private void enqueueCloakedWardTrigger(GameData gameData, Permanent source, UUID controllerId,
                                           StackEntry triggeringEntry) {
        Card wardSourceCard = Card.namedRuntimePlaceholder("Cloaked creature");
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                wardSourceCard,
                controllerId,
                wardSourceCard.getName() + "'s ward ability",
                new ArrayList<>(List.of(new com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect(2))),
                triggeringEntry.getTargetableId(),
                Zone.STACK,
                source.getId()
        );
        entry.setSourcePermanentSnapshot(new Permanent(source));
        entry.setNonTargeting(true);
        enqueueTargetTriggeredAbility(gameData, source, controllerId, source, entry);
    }

    private void collectControllerBecomesTargetOfOpponentTriggers(
            GameData gameData, UUID playerId, StackEntry triggeringEntry) {
        if (playerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return;

        for (Permanent source : new ArrayList<>(battlefield)) {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) continue;

            for (CardEffect effect : TargetWatch.ON_CONTROLLER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.effectsOf(source.getCard())) {
                if (!(effect instanceof CounterUnlessEffect)) continue;

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        playerId,
                        source.getCard().getName() + "'s triggered ability",
                        new ArrayList<>(List.of(effect)),
                        triggeringEntry.getTargetableId(),
                        Zone.STACK,
                        source.getId()
                );
                entry.setSourcePermanentSnapshot(new Permanent(source));
                // This triggered while the targeting ability was being put on the stack, so it goes on the
                // stack after that batch rather than being APNAP-ordered with it (CR 603.3b).
                entry.setTriggerOrderChosen(true);
                gameData.stack.add(entry);

                gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                        "'s triggered ability triggers — counter unless controller pays {1}."));
                log.info("Game {} - {} controller-target counter trigger queued", gameData.id,
                        source.getCard().getName());
            }
        }
    }

    private void collectControllerBecomesTargetOfSpellTriggers(
            GameData gameData, UUID playerId, StackEntry triggeringEntry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return;

        for (Permanent source : new ArrayList<>(battlefield)) {
            List<CardEffect> effects = new ArrayList<>();
            if (!gameQueryService.hasLostPrintedAbilities(gameData, source)) {
                effects.addAll(TargetWatch.ON_CONTROLLER_BECOMES_TARGET_OF_SPELL.effectsOf(source.getCard()));
            }
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source, TargetWatch.ON_CONTROLLER_BECOMES_TARGET_OF_SPELL.trigger()));
            if (effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(source.getCard(), playerId, may, null, source.getId());
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source.getCard(),
                            playerId,
                            source.getCard().getName() + "'s triggered ability",
                            new ArrayList<>(List.of(effect)),
                            null,
                            source.getId()
                    ));
                }
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                        "'s triggered ability triggers."));
                log.info("Game {} - {} controller-becomes-target-of-spell trigger queued",
                        gameData.id, source.getCard().getName());
            }
        }
    }

    /**
     * "Whenever this creature becomes the target of a spell or ability an opponent controls, &lt;non-counter effect&gt;."
     * Handles the non-counter effects in {@link TargetWatch#ON_BECOMES_TARGET_OF_OPPONENT_SPELL} (e.g. "you may
     * draw a card"). Counter/Ward effects on that slot are handled separately by
     * {@link #collectBecomesTargetOfOpponentCounterTriggers}. Called from both the spell and
     * ability paths so the "or ability" clause is honored. Used by Tenured Concocter.
     */
    private void collectBecomesTargetOfOpponentSpellOrAbilityNonCounterTriggers(
            GameData gameData, Permanent source, UUID controllerId, UUID spellOrAbilityControllerId) {
        // Only trigger if the spell/ability is controlled by an opponent
        if (controllerId.equals(spellOrAbilityControllerId)) return;
        if (source.isCloaked()) return;
        if (source.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, source)) return;

        List<CardEffect> effects = TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL.effectsOf(source.getCard()).stream()
                .filter(e -> !(e instanceof CounterUnlessEffect)
                        && !(e instanceof ConditionalEffect conditional
                        && conditional.wrapped() instanceof CounterUnlessEffect))
                .toList();
        if (effects.isEmpty()) return;

        if (effects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
            gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                    source.getCard(), controllerId, new ArrayList<>(effects), source.getId(),
                    controllerId, spellOrAbilityControllerId));
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            return;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                controllerId,
                source.getCard().getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                source.getId()
        );
        gameData.stack.add(entry);

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
        log.info("Game {} - {} becomes-target-of-opponent-spell-or-ability (non-counter) trigger queued",
                gameData.id, source.getCard().getName());
    }

    private void collectBecomesTargetOfOpponentSpellOnlyTriggers(
            GameData gameData, Permanent source, UUID controllerId, UUID spellControllerId) {
        if (controllerId.equals(spellControllerId)) return;

        List<CardEffect> printed = new ArrayList<>(TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY.effectsOf(source.getCard()));
        List<CardEffect> granted = grantedTriggeredAbilitySupport.grantedTriggerEffects(
                gameData, source, TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY.trigger());
        if (printed.isEmpty() && granted.isEmpty()) return;

        List<List<CardEffect>> abilities = new ArrayList<>();
        if (!printed.isEmpty()) {
            abilities.add(printed);
        }
        for (CardEffect grantedEffect : granted) {
            abilities.add(List.of(grantedEffect));
        }
        for (List<CardEffect> ability : abilities) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(ability),
                    null,
                    source.getId()
            );
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} becomes-target-of-opponent-spell-only trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@link TargetWatch#ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY}.
     * Only fires when the targeted permanent is a creature and the spell/ability
     * is controlled by an opponent of the creature's controller.
     */
    private void collectAllyCreatureBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        // Only trigger for creatures
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;
        // Only trigger if the spell/ability is controlled by an opponent
        if (creatureControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            List<CardEffect> effects = new ArrayList<>(TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.effectsOf(source.getCard()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source, TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.trigger()));
            if (effects.isEmpty()) continue;

            List<CardEffect> nonCounterEffects = new ArrayList<>();
            boolean referencesTriggeringObject = false;
            for (CardEffect effect : effects) {
                CardEffect resolved = resolveTriggeringPermanentConditional(
                        gameData, source, creatureControllerId, targetPermanent, effect);
                if (resolved == null) continue;

                if (resolved instanceof CounterSpellingEffect) {
                    StackEntry counterTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source.getCard(),
                            creatureControllerId,
                            source.getCard().getName() + "'s triggered ability",
                            List.of(resolved),
                            triggeringEntry.getTargetableId(),
                            Zone.STACK);
                    counterTrigger.setNonTargeting(true);
                    enqueueTargetTriggeredAbility(gameData, targetPermanent, creatureControllerId, source,
                            counterTrigger);
                    gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                            "'s triggered ability triggers."));
                } else {
                    nonCounterEffects.add(resolved);
                    referencesTriggeringObject |= resolved instanceof TriggeringSpellReferencingEffect;
                }
            }

            List<CardEffect> targetingEffects = nonCounterEffects.stream()
                    .filter(effect -> effect.targetSpec().declaredTarget() != null)
                    .toList();
            List<CardEffect> directEffects = nonCounterEffects.stream()
                    .filter(effect -> effect.targetSpec().declaredTarget() == null)
                    .toList();

            if (!directEffects.isEmpty()) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        creatureControllerId,
                        source.getCard().getName() + "'s triggered ability",
                        directEffects,
                        referencesTriggeringObject ? triggeringEntry.getCard().getId() : null,
                        referencesTriggeringObject ? Zone.STACK : null,
                        source.getId()
                );
                if (referencesTriggeringObject) {
                    entry.setTriggeringCardId(triggeringEntry.getCard().getId());
                    entry.setTriggeringPermanentControllerId(triggeringEntry.getControllerId());
                    if (directEffects.stream().anyMatch(DealDamageToTriggeringSpellControllerEffect.class::isInstance)) {
                        // Only the controller of the triggering spell or ability is referenced, so that
                        // object leaving the stack (an ability that resolved first) must not fizzle the damage.
                        entry.setNonTargeting(true);
                    }
                }
                enqueueTargetTriggeredAbility(gameData, targetPermanent, creatureControllerId, source, entry);

                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            }
            if (!targetingEffects.isEmpty()) {
                TargetFilter targetFilter = null;
                UUID permanentTargetControllerId = creatureControllerId;
                CardEffect declaredTargetEffect = targetingEffects.stream()
                        .filter(effect -> source.getCard().getEffectTargetIndex(effect) >= 0)
                        .findFirst()
                        .orElse(null);
                if (declaredTargetEffect != null) {
                    int targetIndex = source.getCard().getEffectTargetIndex(declaredTargetEffect);
                    if (targetIndex < source.getCard().getTargetGroups().size()) {
                        targetFilter = source.getCard().getTargetGroups().get(targetIndex).getFilter();
                        permanentTargetControllerId = targetFilter instanceof ControlledPermanentPredicateTargetFilter
                                ? null : triggeringEntry.getControllerId();
                    }
                }
                if (targetFilter == null && declaredTargetEffect == null) {
                    PermanentPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                            new PermanentIsCreaturePredicate(),
                            new PermanentControlledBySourceControllerPredicate(),
                            new PermanentNotPredicate(new PermanentIsSpecificPermanentPredicate(targetPermanent.getId()))));
                    targetFilter = new PermanentPredicateTargetFilter(
                            targetPredicate, "Target must be another creature you control");
                }
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        source.getCard(), creatureControllerId, targetingEffects, false,
                        targetFilter,
                        0, source.getId(), new Permanent(source), false,
                        targetPermanent.getId(), permanentTargetControllerId, creatureControllerId));
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                        "'s triggered ability triggers."));
            }
            log.info("Game {} - {} ally-creature-becomes-target-of-opponent trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    /**
     * Checks the spell-only opponent-target trigger slot. This is separate from the
     * spell-or-ability slot because cards such as Swarm Shambler do not trigger from abilities.
     */
    private void collectAllyCreatureBecomesTargetOfOpponentSpellTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry spellEntry) {
        if (!targetPermanent.getCard().hasType(CardType.CREATURE)) return;
        if (creatureControllerId.equals(spellEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            List<CardEffect> effects = new ArrayList<>(TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL.effectsOf(source.getCard()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source, TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL.trigger()));
            if (effects.isEmpty()) continue;

            List<CardEffect> resolvedEffects = new ArrayList<>();
            for (CardEffect effect : effects) {
                CardEffect resolved = resolveTriggeringPermanentConditional(
                        gameData, source, creatureControllerId, targetPermanent, effect);
                if (resolved != null) {
                    resolvedEffects.add(resolved);
                }
            }
            if (resolvedEffects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    creatureControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    resolvedEffects,
                    null,
                    source.getId()
            );
            enqueueTargetTriggeredAbility(gameData, targetPermanent, creatureControllerId, source, entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} ally-creature-becomes-target-of-opponent-spell trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@link TargetWatch#ON_ALLY_CREATURE_BECOMES_TARGET_OF_INSTANT_OR_SORCERY}. Only fires when the
     * targeted permanent is a creature and the targeting spell is an instant or a sorcery; there is
     * no controller restriction, so the creature's controller's own spells trigger it too. The
     * targeted creature is stored as the non-targeting {@code targetId} so the resolved effect can
     * act on it. {@link TriggeringPermanentConditionalEffect} wrappers are filtered against the
     * targeted creature while the trigger event is collected. Used by Wild Defiance and Silverfur
     * Partisan.
     */
    private void collectAllyCreatureBecomesTargetOfInstantOrSorceryTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry spellEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;
        if (!spellEntry.getCard().hasType(CardType.INSTANT) && !spellEntry.getCard().hasType(CardType.SORCERY)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            List<CardEffect> effects = TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_INSTANT_OR_SORCERY.effectsOf(source.getCard());
            if (effects.isEmpty()) continue;

            List<CardEffect> resolvedEffects = new ArrayList<>();
            for (CardEffect effect : effects) {
                CardEffect resolved = resolveTriggeringPermanentConditional(
                        gameData, source, creatureControllerId, targetPermanent, effect);
                if (resolved != null) {
                    resolvedEffects.add(resolved);
                }
            }
            if (resolvedEffects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    creatureControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    resolvedEffects,
                    targetPermanent.getId(),
                    source.getId()
            );
            entry.setNonTargeting(true);
            enqueueTargetTriggeredAbility(gameData, targetPermanent, creatureControllerId, source, entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} ally-creature-becomes-target-of-instant-or-sorcery trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    public void checkCreatureTapForManaTriggers(GameData gameData, UUID tappingPlayerId,
                                                UUID tappedCreatureId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(tappingPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.CreatureTapForMana(tappingPlayerId, tappedCreatureId);
        for (Permanent perm : List.copyOf(battlefield)) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
            for (CardEffect effect : TapWatch.ON_CONTROLLER_TAPS_CREATURE_FOR_MANA.effectsOf(perm.getCard())) {
                var match = new TriggerMatchContext(gameData, perm, tappingPlayerId, effect);
                registry.dispatch(match, TapWatch.ON_CONTROLLER_TAPS_CREATURE_FOR_MANA, effect, ctx);
            }
        }
    }

    public void checkNonlandPermanentTapForManaTriggers(GameData gameData, UUID tappingPlayerId,
                                                        Permanent tappedPermanent,
                                                        Map<ManaColor, Integer> manaBefore,
                                                        boolean manaTypeChoicePending) {
        if (tappedPermanent.getCard().hasType(CardType.LAND)) {
            return;
        }
        UUID tappedPermanentId = tappedPermanent.getId();

        Set<ManaColor> producedManaTypes = manaTypeChoicePending
                ? null
                : newlyProducedManaTypes(manaBefore,
                        gameData.playerManaPools.get(tappingPlayerId).getAllManaTotals());
        if (producedManaTypes != null && producedManaTypes.isEmpty()) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(tappingPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.NonlandPermanentTapForMana(
                tappingPlayerId, tappedPermanentId, producedManaTypes);
        List<TapWatch> watches = tappedPermanent.getCard().isToken() && tappedPermanent.getCard().hasType(CardType.ARTIFACT)
                ? List.of(TapWatch.ON_CONTROLLER_TAPS_NONLAND_PERMANENT_FOR_MANA,
                        TapWatch.ON_CONTROLLER_TAPS_ARTIFACT_TOKEN_FOR_MANA)
                : List.of(TapWatch.ON_CONTROLLER_TAPS_NONLAND_PERMANENT_FOR_MANA);
        for (TapWatch watch : watches) {
            for (Permanent perm : List.copyOf(battlefield)) {
                if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                for (CardEffect effect : watch.effectsOf(perm.getCard())) {
                    var match = new TriggerMatchContext(gameData, perm, tappingPlayerId, effect);
                    registry.dispatch(match, watch, effect, ctx);
                }
            }
        }
    }

    /** Fires triggers that watch any permanent being tapped for mana, including face-up planes. */
    public void checkAnyPlayerTapsPermanentForManaTriggers(GameData gameData, UUID tappingPlayerId,
                                                           UUID tappedPermanentId,
                                                           Map<ManaColor, Integer> manaBefore,
                                                           boolean manaTypeChoicePending) {
        Permanent tappedPermanent = gameQueryService.findPermanentById(gameData, tappedPermanentId);
        if (tappedPermanent == null) {
            return;
        }

        Set<ManaColor> producedManaTypes = manaTypeChoicePending
                ? null
                : newlyProducedManaTypes(manaBefore,
                        gameData.playerManaPools.get(tappingPlayerId).getAllManaTotals());

        var context = new TriggerContext.PermanentTapForMana(
                tappingPlayerId, tappedPermanentId, producedManaTypes);
        gameData.forEachPermanent((controllerId, permanent) ->
                dispatchWatch(gameData, permanent, controllerId,
                        TapWatch.ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA, context));

        if (gameData.planechase == null || gameData.planechase.controllerId == null) {
            return;
        }
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (CardEffect effect : TapWatch.ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA.effectsOf(object.getCard())) {
                registry.dispatch(new TriggerMatchContext(gameData, null,
                                gameData.planechase.controllerId, effect,
                                object.getCard(), object.copy()),
                        TapWatch.ON_ANY_PLAYER_TAPS_PERMANENT_FOR_MANA, effect, context);
            }
        }
    }

    private boolean hasPlanarCreatureCombatDamageToYouTrigger(GameData gameData, UUID damagedPlayerId) {
        return gameData.planechase != null
                && Objects.equals(gameData.planechase.controllerId, damagedPlayerId)
                && gameData.planechase.faceUp.stream()
                .anyMatch(object -> !CombatWatch.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU.effectsOf(object.getCard()).isEmpty());
    }

    private void queuePlanarCreatureCombatDamageToYouTriggers(GameData gameData, UUID damagedPlayerId,
                                                               Permanent sourceCreature) {
        if (!gameQueryService.isCreature(gameData, sourceCreature)
                || gameData.planechase == null
                || !Objects.equals(gameData.planechase.controllerId, damagedPlayerId)) {
            return;
        }

        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, sourceCreature.getId());
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (CardEffect effect : CombatWatch.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU.effectsOf(object.getCard())) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        object.getCard(),
                        damagedPlayerId,
                        object.getCard().getName() + "'s triggered ability",
                        List.of(effect),
                        creatureControllerId,
                        (UUID) null);
                entry.setTriggeringPermanentId(sourceCreature.getId());
                entry.setTriggeringPermanentControllerId(creatureControllerId);
                entry.setSourcePlanarObject(object.copy());
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.cardThen(object.getCard(),
                        "'s combat damage trigger goes on the stack."));
            }
        }
    }

    private static Set<ManaColor> newlyProducedManaTypes(Map<ManaColor, Integer> before,
                                                         Map<ManaColor, Integer> after) {
        EnumSet<ManaColor> produced = EnumSet.noneOf(ManaColor.class);
        for (ManaColor color : ManaColor.values()) {
            if (after.getOrDefault(color, 0) > before.getOrDefault(color, 0)) {
                produced.add(color);
            }
        }
        return produced;
    }

    /**
     * Checks ALL permanents on the targeted creature's controller's battlefield for
     * {@link TargetWatch#ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL}. Only the spell path calls this
     * method, so activated abilities do not trigger it. The card's target groups are reused to
     * choose any optional targets as the triggered ability is put on the stack.
     */
    private void collectAllyCreatureBecomesTargetOfSpellTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, source);
            if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()
                    || gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
            List<CardEffect> effects = new ArrayList<>(TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL.effectsOf(source.getCard()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source, TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL.trigger()));
            if (effects.isEmpty()) continue;

            if (source.getCard().getTargetGroups().size() > 1
                    || etbTokenTargetService.needsSlotBySlotTargetSelection(source.getCard())) {
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        source.getCard(), creatureControllerId, effects, source.getId(), List.of(), 0, 0));
            } else {
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                        source.getCard(), creatureControllerId, effects, source.getId(),
                        source.getCard().getTargetFilter()));
            }

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    "'s triggered ability triggers — choose targets."));
            log.info("Game {} - {} ally-creature-becomes-target-of-spell trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    /**
     * Checks all other permanents on the targeted creature's controller's battlefield for
     * {@link TargetWatch#ON_ANOTHER_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY}. The
     * targeted creature is stored as the non-targeting {@code targetId} of the queued ability.
     */
    private void collectAnotherAllyCreatureBecomesTargetOfSpellOrAbilityTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (source.getId().equals(targetPermanent.getId())
                    || source.isCloaked()
                    || source.isLosesAllAbilitiesUntilEndOfTurn()) continue;

            List<CardEffect> effects = new ArrayList<>(TargetWatch.ON_ANOTHER_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY.effectsOf(source.getCard()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source, TargetWatch.ON_ANOTHER_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY.trigger()));
            if (effects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    creatureControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    effects,
                    targetPermanent.getId(),
                    source.getId());
            entry.setNonTargeting(true);
            enqueueTargetTriggeredAbility(gameData, targetPermanent, creatureControllerId, source, entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} another-ally-creature-becomes-target trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    /**
     * Checks ALL permanents across every battlefield for
     * {@link TargetWatch#ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY}. Only fires when the
     * targeted permanent is a creature. The targeted creature is stored as the non-targeting
     * {@code targetId} so the resolved effect can act on it. Used by Cowardice.
     */
    private void collectAnyCreatureBecomesTargetTriggers(
            GameData gameData, Permanent targetPermanent, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;

        UUID triggeringSourceControllerId = triggeringEntry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentController(gameData, triggeringEntry.getSourcePermanentId());

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;

            for (Permanent source : battlefield) {
                if (gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;
                List<CardEffect> effects = TargetWatch.ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY.effectsOf(source.getCard());
                if (effects.isEmpty()) continue;

                List<CardEffect> resolvedEffects = effects.stream()
                        .map(effect -> effect instanceof TriggeringPermanentConditionalEffect conditional
                                ? predicateEvaluationService.matchesPermanentPredicate(targetPermanent,
                                conditional.predicate(), FilterContext.of(gameData)
                                .withSourceControllerId(playerId).withSourcePermanentId(source.getId()))
                                ? conditional.wrapped() : null : effect)
                        .filter(Objects::nonNull)
                        .map(effect -> effect.resolveForBecomesTargetOfSpellOrAbility(
                                triggeringEntry,
                                source.getId(),
                                targetPermanent.getId(),
                                playerId,
                                triggeringSourceControllerId))
                        .filter(Objects::nonNull)
                        .toList();
                if (resolvedEffects.isEmpty()) continue;

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source.getCard(),
                        playerId,
                        source.getCard().getName() + "'s triggered ability",
                        new ArrayList<>(resolvedEffects),
                        targetPermanent.getId(),
                        source.getId()
                );
                entry.setNonTargeting(true);
                UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());
                Runnable enqueue = () -> enqueueTargetTriggeredAbility(gameData, targetPermanent,
                        targetControllerId, source, entry);
                if (playerId.equals(targetControllerId)) {
                    collectWithAllyCreatureBecomesTargetCopies(gameData, targetPermanent, targetControllerId, enqueue);
                } else {
                    enqueue.run();
                }

                gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
                log.info("Game {} - {} any-creature-becomes-target trigger queued",
                        gameData.id, source.getCard().getName());
            }
        }
    }

    /**
     * Checks the spell/ability controller's battlefield for
     * {@link TargetWatch#ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY}. Only fires
     * when the targeted permanent is a creature controlled by an opponent of the spell/ability's
     * controller. The targeted creature is stored as the non-targeting {@code targetId} and the
     * listening permanent as the {@code sourcePermanentId}. Used by Willbreaker.
     */
    private void collectOpponentCreatureBecomesTargetOfYourSpellTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;

        UUID triggeringControllerId = triggeringEntry.getControllerId();
        if (triggeringControllerId == null || triggeringControllerId.equals(creatureControllerId)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(triggeringControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            List<CardEffect> effects = TargetWatch.ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY.effectsOf(source.getCard());
            if (effects.isEmpty() || gameQueryService.hasLostPrintedAbilities(gameData, source)) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    triggeringControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(effects),
                    targetPermanent.getId(),
                    source.getId()
            );
            entry.setNonTargeting(true);
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} opponent-creature-becomes-target-of-your-spell trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    public void checkDealtDamageToCreatureTriggers(GameData gameData, Permanent damagedCreature, int damageDealt,
                                                   UUID damageSourceControllerId) {
        checkDealtDamageToCreatureTriggers(gameData, damagedCreature, damageDealt, damageSourceControllerId,
                null, null);
    }

    public void checkDealtDamageToCreatureTriggers(GameData gameData, Permanent damagedCreature, int damageDealt,
                                                   UUID damageSourceControllerId, Card sourceCard,
                                                   UUID sourcePermanentId) {
        if (damageDealt > 0) {
            checkEnchantedCreatureDealtDamageTriggers(gameData, damagedCreature, damageDealt);
        }

        checkDamageToCreatureTriggers(gameData, damagedCreature, damageDealt, damageSourceControllerId,
                sourceCard, sourcePermanentId, DamageWatch.ON_DEALT_DAMAGE);
    }

    /** Fires triggers whose event specifically requires noncombat damage dealt to this permanent. */
    public void checkNoncombatDamageToSelfTriggers(GameData gameData, Permanent damagedCreature,
                                                   int damageDealt, UUID damageSourceControllerId,
                                                   Card sourceCard, UUID sourcePermanentId) {
        checkDamageToCreatureTriggers(gameData, damagedCreature, damageDealt, damageSourceControllerId,
                sourceCard, sourcePermanentId, DamageWatch.ON_NONCOMBAT_DAMAGE_TO_SELF);
    }

    private void checkDamageToCreatureTriggers(GameData gameData, Permanent damagedCreature, int damageDealt,
                                               UUID damageSourceControllerId, Card sourceCard,
                                               UUID sourcePermanentId, DamageWatch watch) {
        List<CardEffect> effects = new ArrayList<>();
        if (!damagedCreature.isFaceDown()
                && !gameQueryService.hasLostPrintedAbilities(gameData, damagedCreature)) {
            effects.addAll(watch.effectsOf(damagedCreature.getCard()));
        }
        effects.addAll(damagedCreature.getTemporaryTriggerEffects(watch.trigger()));
        effects.addAll(damagedCreature.getPersistentTriggerEffects(watch.trigger()));
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                gameData, damagedCreature, watch.trigger()));
        if (effects.isEmpty()) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        if (controllerId == null) return;

        var ctx = new TriggerContext.DamageToCreature(damagedCreature, damageDealt, damageSourceControllerId,
                sourceCard, sourcePermanentId);

        for (CardEffect effect : effects) {
            CardEffect authoredEffect = effect instanceof PerDamageSourceTriggerEffect perSource
                    ? perSource.effect() : effect;
            CardEffect dispatchedEffect = OncePerTurnTriggerSupport.unwrapIfAvailable(
                    gameData, damagedCreature, authoredEffect);
            if (dispatchedEffect == null) continue;

            var match = new TriggerMatchContext(gameData, damagedCreature, controllerId, dispatchedEffect);
            boolean triggered = dispatch(match, watch, dispatchedEffect, ctx);
            if (triggered) {
                OncePerTurnTriggerSupport.markIfNeeded(gameData, damagedCreature, authoredEffect);
            }
        }
    }

    // ── Enchanted-creature-dealt-damage triggers ───────────────────────

    public void checkEnchantedCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature, int damageDealt) {
        if (damageDealt <= 0) return;

        var ctx = new TriggerContext.DamageToCreature(damagedCreature, damageDealt, null);

        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(damagedCreature.getId())) {
                return;
            }
            for (CardEffect effect : DamageWatch.ON_ENCHANTED_CREATURE_DEALT_DAMAGE.effectsOf(perm.getCard())) {
                var match = new TriggerMatchContext(gameData, perm, auraOwnerId, effect);
                dispatch(match, DamageWatch.ON_ENCHANTED_CREATURE_DEALT_DAMAGE, effect, ctx);
            }
        });
    }

    // ── Opponent-creature-dealt-damage triggers ──────────────────────────

    /**
     * Fires ON_OPPONENT_CREATURE_DEALT_DAMAGE triggers on permanents whose controller
     * is different from the damaged creature's controller (i.e. the damaged creature is
     * an opponent's creature from the perspective of the permanent's controller).
     * Called once per damaged creature — each call produces one trigger per listening permanent.
     */
    public void checkEnchantedPlayerDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            int damageDealt) {
        if (damageDealt <= 0) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, damageDealt);
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !damagedPlayerId.equals(perm.getAttachedTo())) {
                return;
            }
            for (CardEffect effect : DamageWatch.ON_ENCHANTED_PLAYER_DEALT_DAMAGE.effectsOf(perm.getCard())) {
                var match = new TriggerMatchContext(gameData, perm, auraOwnerId, effect);
                dispatch(match, DamageWatch.ON_ENCHANTED_PLAYER_DEALT_DAMAGE, effect, ctx);
            }
        });
    }

    public void checkOpponentCreatureDealtDamageTriggers(GameData gameData, UUID damagedCreatureControllerId) {
        gameData.forEachPermanent((playerId, perm) -> {
            // Only fire when the damaged creature was controlled by an opponent of this permanent's controller
            if (playerId.equals(damagedCreatureControllerId)) return;

            List<CardEffect> effects = DamageWatch.ON_OPPONENT_CREATURE_DEALT_DAMAGE.effectsOf(perm.getCard());
            if (effects == null || effects.isEmpty()) return;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers (opponent creature dealt damage)", gameData.id, perm.getCard().getName());
            }
        });
    }

    /** Fires turn-scoped global triggers for damage dealt to an opponent's creature. */
    public void checkTemporaryGlobalOpponentCreatureDealtDamageTriggers(
            GameData gameData, Permanent damagedCreature, UUID damagedCreatureControllerId, int damageDealt) {
        if (damagedCreature == null || damagedCreatureControllerId == null || damageDealt <= 0) return;

        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(DamageWatch.ON_OPPONENT_CREATURE_DEALT_DAMAGE.trigger())
                    || watcher.controllerId().equals(damagedCreatureControllerId)) {
                continue;
            }
            enqueueTemporaryGlobalTrigger(gameData, watcher, damagedCreature.getId(), damageDealt);
        }
    }

    /**
     * Fires excess-damage triggers on permanents whose controller is different from the damaged
     * permanent's controller. Triggers from the {@code ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_NONCOMBAT_DAMAGE}
     * slot fire only when {@code noncombatDamage} is true; combat excess damage still records the excess.
     */
    public void checkOpponentPermanentDealtExcessDamageTriggers(GameData gameData,
                                                                Permanent damagedPermanent,
                                                                UUID damagedPermanentControllerId,
                                                                int excessDamage,
                                                                boolean noncombatDamage) {
        if (damagedPermanent == null || damagedPermanentControllerId == null || excessDamage <= 0
                || (!gameQueryService.isCreature(gameData, damagedPermanent)
                && !damagedPermanent.getCard().hasType(CardType.PLANESWALKER))) return;

        gameData.recordControllerOfPermanentDealtExcessDamageThisTurn(damagedPermanentControllerId);

        if (gameQueryService.isCreature(gameData, damagedPermanent)) {
            gameData.recordPermanentDealtExcessDamageThisTurn(damagedPermanent.getId());
        }

        List<DamageWatch> watches = noncombatDamage
                ? List.of(DamageWatch.ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_DAMAGE,
                        DamageWatch.ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_NONCOMBAT_DAMAGE)
                : List.of(DamageWatch.ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_DAMAGE);
        TriggerContext context = new TriggerContext.OpponentPermanentDealtExcessDamage(
                damagedPermanent, damagedPermanentControllerId, excessDamage);
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(damagedPermanentControllerId)) return;

            List<CardEffect> effects = new ArrayList<>();
            for (DamageWatch watch : watches) {
                effects.addAll(watch.effectsOf(perm.getCard()));
                effects.addAll(perm.getTemporaryTriggerEffects(watch.trigger()));
                effects.addAll(perm.getPersistentTriggerEffects(watch.trigger()));
            }
            for (CardEffect effect : effects) {
                if (effect instanceof ConditionalEffect conditional && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(perm, playerId).withTargetId(damagedPermanent.getId()))) {
                    continue;
                }
                if (effect instanceof DealDamageToAnyTargetEffect) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            perm.getCard(), playerId, new ArrayList<>(List.of(effect)), false,
                            new AnyTargetPredicateTargetFilter(
                                    new PermanentNotPredicate(
                                            new PermanentIsSpecificPermanentPredicate(damagedPermanent.getId())),
                                    new PlayerRelationPredicate(PlayerRelation.ANY),
                                    "Target must be other than the permanent dealt excess damage."),
                            excessDamage, perm.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers (opponent permanent dealt excess damage)",
                            gameData.id, perm.getCard().getName());
                    continue;
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        damagedPermanent.getId(),
                        perm.getId());
                entry.setNonTargeting(true);
                entry.setEventValue(excessDamage);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers (opponent permanent dealt excess damage)",
                        gameData.id, perm.getCard().getName());
            }
        });
    }

    public void checkOpponentCreatureDealtExcessNoncombatDamageTriggers(GameData gameData,
                                                                          StackEntry damageEntry,
                                                                          Permanent damagedCreature,
                                                                          UUID damagedCreatureControllerId,
                                                                          int excessDamage) {
        if (damageEntry == null || damagedCreature == null || damagedCreatureControllerId == null
                || excessDamage <= 0 || !gameQueryService.isCreature(gameData, damagedCreature)) return;

        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(damagedCreatureControllerId)) return;

            List<CardEffect> effects = new ArrayList<>(DamageWatch.ON_OPPONENT_CREATURE_DEALT_EXCESS_NONCOMBAT_DAMAGE.effectsOf(perm.getCard()));
            effects.addAll(perm.getTemporaryTriggerEffects(DamageWatch.ON_OPPONENT_CREATURE_DEALT_EXCESS_NONCOMBAT_DAMAGE.trigger()));
            effects.addAll(perm.getPersistentTriggerEffects(DamageWatch.ON_OPPONENT_CREATURE_DEALT_EXCESS_NONCOMBAT_DAMAGE.trigger()));
            for (CardEffect effect : effects) {
                if (!damageEntry.markNoncombatExcessDamageTriggerFired(perm.getId(), effect)) continue;
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        playerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        damagedCreature.getId(),
                        perm.getId());
                entry.setNonTargeting(true);
                gameData.stack.add(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers (opponent creature dealt excess noncombat damage)",
                        gameData.id, perm.getCard().getName());
            }
        });
    }

    // ── Ally-creature-deals-damage-to-creature reflection (Greatbow Doyen) ──

    /**
     * Fires ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE triggers. When a creature the watcher's
     * controller controls (matching the effect's source filter) deals damage to a creature, the
     * damage-source creature deals that much damage to the damaged creature's controller. Called
     * once per source/target/damage event; {@code combatDamage} tells the combat-only listeners
     * (Sosuke, Son of Seshiro) whether this event was combat damage.
     */
    public void checkAllyDealtDamageToCreatureTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId,
            Permanent damagedCreature, int damage, boolean combatDamage) {
        checkWatchersDealtDamageToCreatureTriggers(gameData, damageSource, damageSourceControllerId,
                damagedCreatureControllerId, damagedCreatureId, damagedCreature, damage, combatDamage);
        checkSelfDealtDamageToCreatureTriggers(gameData, damageSource, damageSourceControllerId,
                damagedCreatureControllerId, damagedCreatureId, damagedCreature, damage, combatDamage);
    }

    /**
     * Fires the ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE triggers that other permanents watch (the
     * source's controller's creatures, and equipment on the source). Self-scoped triggers are fired
     * separately by {@link #checkSelfDealtDamageToCreatureTriggers}.
     */
    public void checkWatchersDealtDamageToCreatureTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId,
            Permanent damagedCreature, int damage, boolean combatDamage) {
        if (damageSource == null || damageSourceControllerId == null || damagedCreatureControllerId == null || damage <= 0) {
            return;
        }

        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            // The damage source watches itself below, whether or not it survived the damage.
            if (watcher.getId().equals(damageSource.getId())) return;
            // "a creature you control" — the damage source must be controlled by the watcher's controller.
            // Watchers of creatures under any player's control (Toxin Sliver) are handled separately.
            if (!watcherControllerId.equals(damageSourceControllerId)) {
                fireAnySourceDealtDamageToCreatureTriggers(gameData, watcher, watcherControllerId, damageSource,
                        damagedCreatureId, damage, combatDamage);
                return;
            }

            fireAllyDealtDamageToCreatureTrigger(gameData, watcher, damageSource, damageSourceControllerId,
                    damagedCreatureControllerId, damagedCreatureId, damagedCreature, damage, combatDamage, false);
        });

        // "Equipped creature" triggers (Giant's Skewer) are controlled by the equipment's controller,
        // which may be the opponent of the creature that dealt the damage.
        gameData.forEachPermanent((equipmentControllerId, equipment) -> {
            if (equipmentControllerId.equals(damageSourceControllerId)) return;
            if (!isEquippedBy(gameData, equipment, damageSource)) return;

            fireAllyDealtDamageToCreatureTrigger(gameData, equipment, damageSource, equipmentControllerId,
                    damagedCreatureControllerId, damagedCreatureId, damagedCreature, damage, combatDamage, true);
        });
    }

    /**
     * Fires the self-scoped ("Whenever this creature deals damage to a creature, …") triggers of the
     * damage source, including ones it has been granted. Combat damage calls this before
     * state-based actions so the grant is still present when lethal damage back from the blocker or a
     * granting permanent dying in the same step has already moved the source off the battlefield.
     */
    public void checkSelfDealtDamageToCreatureTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId,
            Permanent damagedCreature, int damage, boolean combatDamage) {
        if (damageSource == null || damageSourceControllerId == null || damagedCreatureControllerId == null || damage <= 0) {
            return;
        }

        fireAllyDealtDamageToCreatureTrigger(gameData, damageSource, damageSource, damageSourceControllerId,
                damagedCreatureControllerId, damagedCreatureId, damagedCreature, damage, combatDamage, false);
    }

    /** Fires triggers watching a creature controlled by a player's creatures fighting. */
    public void checkAllyCreatureFightsTriggers(GameData gameData, Permanent fightingCreature,
                                                 UUID fightingCreatureControllerId) {
        if (fightingCreature == null || fightingCreatureControllerId == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(fightingCreatureControllerId);
        if (battlefield != null) {
            for (Permanent watcher : new ArrayList<>(battlefield)) {
                if (watcher.getId().equals(fightingCreature.getId())) continue;
                dispatchWatch(gameData, watcher, fightingCreatureControllerId,
                        ActionWatch.ON_ALLY_CREATURE_FIGHTS,
                        new TriggerContext.CreatureFights(fightingCreature));
            }
        }

        // A fighting creature's own trigger still triggers if it dies in the fight.
        dispatchWatch(gameData, fightingCreature, fightingCreatureControllerId,
                ActionWatch.ON_ALLY_CREATURE_FIGHTS,
                new TriggerContext.CreatureFights(fightingCreature));
    }

    public void checkAllyDealtDamageToPlaneswalkerTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedPlaneswalkerId, int damage, boolean combatDamage,
            List<StackEntry> deferredTriggers) {
        checkCreatureDealsDamageToEnchantedPlaneswalkerTriggers(
                gameData, damageSource, damagedPlaneswalkerId, damage, combatDamage, deferredTriggers);
        if (damageSource == null || damageSourceControllerId == null || damagedPlaneswalkerId == null || damage <= 0) {
            return;
        }
        Permanent planeswalker = gameQueryService.findPermanentById(gameData, damagedPlaneswalkerId);
        if (planeswalker == null || !planeswalker.getCard().hasType(CardType.PLANESWALKER)
                || !gameQueryService.isCreature(gameData, damageSource)) return;

        TriggerContext context = new TriggerContext.CreatureDealsDamageToPlaneswalker(
                damageSource, damagedPlaneswalkerId, damage, combatDamage, deferredTriggers);
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!watcherControllerId.equals(damageSourceControllerId)) return;
            List<CardEffect> effects = new ArrayList<>(
                    DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER.effectsOf(watcher.getCard()));
            effects.addAll(watcher.getTemporaryTriggerEffects(DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER.trigger()));
            effects.addAll(watcher.getPersistentTriggerEffects(DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER.trigger()));
            for (CardEffect effect : effects) {
                TriggerMatchContext match = new TriggerMatchContext(gameData, watcher, watcherControllerId, effect);
                registry.dispatch(match, DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER, effect, context);
            }
        });
    }

    public void checkCreatureDealsDamageToEnchantedPlaneswalkerTriggers(GameData gameData, Permanent damageSource,
            UUID damagedPlaneswalkerId, int damage, boolean combatDamage, List<StackEntry> deferredTriggers) {
        if (damageSource == null || damagedPlaneswalkerId == null || damage <= 0
                || !gameQueryService.isCreature(gameData, damageSource)) {
            return;
        }
        Permanent planeswalker = gameQueryService.findPermanentById(gameData, damagedPlaneswalkerId);
        if (planeswalker == null || !planeswalker.getCard().hasType(CardType.PLANESWALKER)) {
            return;
        }

        TriggerContext context = new TriggerContext.CreatureDealsDamageToPlaneswalker(
                damageSource, damagedPlaneswalkerId, damage, combatDamage, deferredTriggers);
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            List<CardEffect> effects = new ArrayList<>(
                    DamageWatch.ON_CREATURE_DEALS_DAMAGE_TO_ENCHANTED_PLANESWALKER.effectsOf(watcher.getCard()));
            effects.addAll(watcher.getTemporaryTriggerEffects(DamageWatch.ON_CREATURE_DEALS_DAMAGE_TO_ENCHANTED_PLANESWALKER.trigger()));
            effects.addAll(watcher.getPersistentTriggerEffects(DamageWatch.ON_CREATURE_DEALS_DAMAGE_TO_ENCHANTED_PLANESWALKER.trigger()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, watcher, watcherControllerId,
                    DamageWatch.ON_CREATURE_DEALS_DAMAGE_TO_ENCHANTED_PLANESWALKER.trigger()));
            for (CardEffect effect : effects) {
                TriggerMatchContext match = new TriggerMatchContext(gameData, watcher, watcherControllerId, effect);
                registry.dispatch(match, DamageWatch.ON_CREATURE_DEALS_DAMAGE_TO_ENCHANTED_PLANESWALKER,
                        effect, context);
            }
        });
    }

    public void queueSourceDealsCombatDamageToPlayerOrPlaneswalkerTriggers(GameData gameData, Card sourceCard,
                                                                            UUID sourceControllerId,
                                                                            UUID sourcePermanentId,
                                                                            int totalDamage,
                                                                            int damageToPlayers,
                                                                            List<CardEffect> snapshottedEffects) {
        if (sourceCard == null || sourceControllerId == null || sourcePermanentId == null || totalDamage <= 0) return;
        dispatchSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, snapshottedEffects,
                CombatWatch.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER);
    }

    public void queueSourceDealsCombatDamageToPlayerOrBattleTriggers(GameData gameData, Card sourceCard,
                                                                       UUID sourceControllerId,
                                                                       UUID sourcePermanentId,
                                                                       int totalDamage,
                                                                       int damageToPlayers,
                                                                       List<CardEffect> snapshottedEffects) {
        if (sourceCard == null || sourceControllerId == null || sourcePermanentId == null || totalDamage <= 0) return;
        dispatchSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, snapshottedEffects,
                CombatWatch.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE);
        queueEquippedCreatureDealsCombatDamageToPlayerOrBattleTriggers(
                gameData, sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers);
    }

    private void dispatchSourceDealsCombatDamageTriggers(GameData gameData, Card sourceCard,
                                                         UUID sourceControllerId, UUID sourcePermanentId,
                                                         int totalDamage, int damageToPlayers,
                                                         List<CardEffect> snapshottedEffects,
                                                         CombatWatch watch) {
        var ctx = new TriggerContext.SourceDealsCombatDamage(
                sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers);
        List<CardEffect> effects = new ArrayList<>(watch.effectsOf(sourceCard));
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (snapshottedEffects != null) {
            effects.addAll(snapshottedEffects);
        } else if (sourcePermanent != null) {
            effects.addAll(sourcePermanent.getTemporaryTriggerEffects(watch.trigger()));
            effects.addAll(sourcePermanent.getPersistentTriggerEffects(watch.trigger()));
        }
        for (CardEffect effect : effects) {
            var match = new TriggerMatchContext(gameData, sourcePermanent, sourceControllerId, effect);
            dispatch(match, watch, effect, ctx);
        }
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(watch.trigger())
                    || !sourcePermanentId.equals(watcher.watchedPermanentId())) continue;
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(watcher.effect())),
                    null,
                    sourcePermanentId);
            entry.setEventValue(totalDamage);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /**
     * Fires the {@link DestroyCreatureDamagedByMatchingCreatureEffect} triggers of a watcher that cares about
     * damage dealt by creatures under any player's control. The ability is controlled by the watcher's
     * controller, not by the damage source's.
     */
    private void fireAnySourceDealtDamageToCreatureTriggers(GameData gameData, Permanent watcher,
            UUID watcherControllerId, Permanent damageSource, UUID damagedCreatureId, int damage,
            boolean combatDamage) {
        if (watcher.isFaceDown() || watcher.isLosesAllAbilitiesUntilEndOfTurn()
                || gameQueryService.hasLostPrintedAbilities(gameData, watcher)) {
            return;
        }
        for (CardEffect effect : DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE.effectsOf(watcher.getCard())) {
            if (effect instanceof DestroyCreatureDamagedByMatchingCreatureEffect destroy) {
                queueAnySourceDestroyTrigger(gameData, watcher, watcherControllerId, destroy, damageSource,
                        damagedCreatureId, combatDamage);
            }
        }
    }

    private void queueAnySourceDestroyTrigger(GameData gameData, Permanent watcher, UUID watcherControllerId,
            DestroyCreatureDamagedByMatchingCreatureEffect destroy, Permanent damageSource,
            UUID damagedCreatureId, boolean combatDamage) {
        if (damagedCreatureId == null || (destroy.combatDamageOnly() && !combatDamage)) {
            return;
        }
        if (destroy.sourceFilter() != null
                && !predicateEvaluationService.matchesPermanentPredicate(damageSource, destroy.sourceFilter(),
                FilterContext.of(gameData).withSourcePermanentSnapshot(watcher))) {
            return;
        }
        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                watcherControllerId,
                watcher.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DestroyTargetPermanentEffect(destroy.cannotBeRegenerated()))),
                damagedCreatureId,
                watcher.getId());
        trigger.setNonTargeting(true);
        gameData.stack.add(trigger);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
        log.info("Game {} - {} destroys the creature dealt damage by {}", gameData.id,
                watcher.getCard().getName(), damageSource.getCard().getName());
    }

    private void fireAllyDealtDamageToCreatureTrigger(GameData gameData, Permanent watcher, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId,
            Permanent damagedCreature, int damage, boolean combatDamage, boolean equipmentScopedOnly) {
        if (watcher.isLosesAllAbilitiesUntilEndOfTurn() || gameQueryService.hasLostPrintedAbilities(gameData, watcher)) {
            return;
        }
        // A face-down permanent has no printed abilities (CR 708.2).
        List<CardEffect> effects = watcher.isFaceDown()
                ? new ArrayList<>()
                : new ArrayList<>(DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE.effectsOf(watcher.getCard()));
        // Abilities granted until end of turn (Cruel Deceiver) live on the permanent, not the card.
        effects.addAll(watcher.getTemporaryTriggerEffects(DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE.trigger()));
        if (watcher.getId().equals(damageSource.getId())) {
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, watcher, damageSourceControllerId,
                    DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE.trigger()));
        }

        TriggerContext context = new TriggerContext.CreatureDealsDamageToCreature(
                damageSource, damagedCreatureId, damage, combatDamage,
                damagedCreature, damagedCreatureControllerId);

        for (CardEffect effect : effects) {
            // An attached permanent controlled by the damaged creature's opponent only contributes its
            // equipped-creature triggers; its ally-scoped listeners watch only their own controller's creatures.
            if (equipmentScopedOnly && !(effect instanceof EquipmentDamagesOtherDefendingCreaturesEffect)
                    && !(effect instanceof EquipmentTapsAndLocksDamagedCreatureEffect)
                    && !(effect instanceof DamagedCreatureTriggerEffect equipped && equipped.equipmentScoped())) {
                continue;
            }
            if (effect instanceof DestroyCreatureDamagedByMatchingCreatureEffect destroy) {
                queueAnySourceDestroyTrigger(gameData, watcher, damageSourceControllerId, destroy, damageSource,
                        damagedCreatureId, combatDamage);
                continue;
            }
            TriggerMatchContext match = new TriggerMatchContext(gameData, watcher, damageSourceControllerId, effect);
            if (dispatch(match, DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE, effect, context)) {
                continue;
            }
            if (effect instanceof ReflectAllyDamageToDamagedCreatureControllerEffect reflect) {
                if (reflect.combatOnly() && !combatDamage) continue;
                if (reflect.sourceMustBeWatcher() && !watcher.getId().equals(damageSource.getId())) continue;
                if (!reflect.sourceMustBeWatcher() && reflect.sourceFilter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(gameData, damageSource, reflect.sourceFilter())) {
                    continue;
                }

                // The normal form deals damage; Flayed Nim's self-scoped form causes life loss.
                CardEffect triggeredEffect = reflect.lifeLoss()
                        ? new LoseLifeEffect(damage, LoseLifeRecipient.TARGET_PLAYER)
                        : new DealDamageToPlayersEffect(damage, DamageRecipient.TARGET_PLAYER);
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        damageSource.getCard(),
                        damageSourceControllerId,
                        damageSource.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(triggeredEffect)),
                        damagedCreatureControllerId,
                        damageSource.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                if (reflect.lifeLoss()) {
                    log.info("Game {} - {} causes {} life loss to {}", gameData.id,
                            watcher.getCard().getName(), damage,
                            gameData.playerIdToName.get(damagedCreatureControllerId));
                } else {
                    log.info("Game {} - {} reflects {} damage to {}", gameData.id,
                            watcher.getCard().getName(), damage,
                            gameData.playerIdToName.get(damagedCreatureControllerId));
                }
            } else if (effect instanceof DamageDamagedCreatureControllerAndSelfEffect punisher) {
                // "this creature" — fire only when the watcher itself dealt the damage.
                if (!watcher.getId().equals(damageSource.getId())) continue;

                // This creature deals N damage to that creature's controller and M damage to you.
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        damageSource.getCard(),
                        damageSourceControllerId,
                        damageSource.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(
                                new com.github.laxika.magicalvibes.model.effect.DealDamageToCombatOpponentControllerEffect(
                                        new com.github.laxika.magicalvibes.model.amount.Fixed(punisher.amountToDamagedCreatureController())),
                                new DealDamageToPlayersEffect(punisher.amountToSelf(), DamageRecipient.CONTROLLER))),
                        damagedCreatureId,
                        damageSource.getId()
                );
                trigger.setNonTargeting(true);
                trigger.setTriggeringPermanentControllerId(damagedCreatureControllerId);
                trigger.setSourcePermanentSnapshot(new Permanent(damageSource));
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} deals {} to {} and {} to its controller", gameData.id,
                        watcher.getCard().getName(), punisher.amountToDamagedCreatureController(),
                        gameData.playerIdToName.get(damagedCreatureControllerId), punisher.amountToSelf());
            } else if (effect instanceof DamagedCreatureTriggerEffect damagedCreatureTrigger) {
                if (damagedCreatureTrigger.combatDamageOnly() && !combatDamage) continue;
                Permanent triggerSource = damageSource;
                if (damagedCreatureTrigger.equipmentScoped()) {
                    if (!isEquippedBy(gameData, watcher, damageSource)) continue;
                    triggerSource = watcher;
                } else if (!watcher.getId().equals(damageSource.getId())) {
                    // "this creature" — fire only when the watcher itself dealt the damage.
                    continue;
                }
                if (damagedCreatureId == null) continue;
                if (damagedCreatureTrigger.requiresExcessDamage()
                        && !gameData.permanentsDealtExcessDamageThisTurn.contains(damagedCreatureId)) {
                    continue;
                }
                var damagedCreatureFilter = damagedCreatureTrigger.damagedCreatureFilter();
                if (damagedCreatureFilter != null && (damagedCreature == null
                        || !predicateEvaluationService.matchesPermanentPredicate(
                        gameData, damagedCreature, damagedCreatureFilter))) {
                    continue;
                }

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        triggerSource.getCard(),
                        damageSourceControllerId,
                        triggerSource.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(damagedCreatureTrigger.triggeredEffect())),
                        damagedCreatureId,
                        triggerSource.getId()
                );
                if (damagedCreatureTrigger.useDamagedCreatureAsTriggeringPermanent()) {
                    trigger.setTriggeringPermanentId(damagedCreatureId);
                    trigger.setTriggeringPermanentControllerId(damagedCreatureControllerId);
                }
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} will resolve its damaged-creature trigger", gameData.id,
                        watcher.getCard().getName());
            } else if (effect instanceof TapAndSkipUntapDamagedCreatureEffect) {
                // "this creature" — fire only when the watcher itself dealt the damage.
                if (!watcher.getId().equals(damageSource.getId())) continue;
                if (damagedCreatureId == null) continue;

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        damageSource.getCard(),
                        damageSourceControllerId,
                        damageSource.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                new SkipNextUntapEffect(TapUntapScope.TARGET))),
                        damagedCreatureId,
                        damageSource.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} taps and locks the creature it damaged", gameData.id,
                        watcher.getCard().getName());
            } else if (effect instanceof EquipmentTapsAndLocksDamagedCreatureEffect) {
                // "equipped creature" — the watcher is the Equipment, so it must be attached to
                // the creature that dealt the damage. Last-known attachment is valid when that
                // creature died to the same damage event.
                if (!isEquippedBy(gameData, watcher, damageSource)) continue;
                if (damagedCreatureId == null) continue;

                UUID watcherControllerId = gameData.findControllerOf(watcher);
                if (watcherControllerId == null) continue;

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(),
                        watcherControllerId,
                        watcher.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                DoesntUntapEffect.targetWhileSourceOnBattlefield())),
                        damagedCreatureId,
                        watcher.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} taps and locks the creature damaged by its equipped creature",
                        gameData.id, watcher.getCard().getName());
            } else if (effect instanceof DestroyDamagedCreatureAtEndOfCombatEffect delayedDestroy) {
                if (delayedDestroy.combatDamageOnly() && !combatDamage) continue;
                if (delayedDestroy.selfOnly() && !watcher.getId().equals(damageSource.getId())) continue;
                if (damagedCreatureId == null) continue;
                if (delayedDestroy.sourceFilter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        damageSource,
                        delayedDestroy.sourceFilter(),
                        FilterContext.of(gameData).withSourcePermanentSnapshot(watcher))) {
                    continue;
                }

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(),
                        damageSourceControllerId,
                        watcher.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(new DestroyTargetPermanentAtEndOfCombatEffect())),
                        damagedCreatureId,
                        watcher.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} schedules the damaged creature for destruction at end of combat",
                        gameData.id, watcher.getCard().getName());
            } else if (effect instanceof EquipmentDamagesOtherDefendingCreaturesEffect) {
                // "equipped creature" — the watcher is the Equipment, so it must be attached to
                // the creature that dealt the damage. The ability triggered when the damage was
                // dealt, so a host that has already died to the same combat damage (which detaches
                // the Equipment) still counts, read from last-known attachment.
                if (!isEquippedBy(gameData, watcher, damageSource)) continue;
                if (damagedCreatureId == null) continue;
                if (!wasBlocking(gameData, damageSource, damagedCreatureId, combatDamage)) continue;

                // The Equipment deals that much damage to each other creature defending player controls.
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(),
                        damageSourceControllerId,
                        watcher.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(new DealDamageToEachMatchingPermanentEffect(damage,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentIsSpecificPermanentPredicate(damagedCreatureId)))),
                                EachPermanentScope.TARGET_PLAYER))),
                        damagedCreatureControllerId,
                        watcher.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);

                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} deals {} damage to each other creature {} controls", gameData.id,
                        watcher.getCard().getName(), damage,
                        gameData.playerIdToName.get(damagedCreatureControllerId));
            }
        }
    }

    /**
     * Whether the creature that was just dealt damage was a blocking creature. Lethal damage may
     * already have moved it off the battlefield, in which case the fallback reads the damage source:
     * a creature only deals combat damage to creatures blocking it while it is attacking.
     */
    private boolean isEquippedBy(GameData gameData, Permanent equipment, Permanent host) {
        return isEquippedBy(gameData, equipment, host.getId());
    }

    private boolean isEquippedBy(GameData gameData, Permanent equipment, UUID hostId) {
        if (hostId.equals(equipment.getAttachedTo())) return true;
        // Only a host that already left the battlefield may be matched on last-known attachment.
        return hostId.equals(equipment.getLastAttachedTo())
                && gameQueryService.findPermanentById(gameData, hostId) == null;
    }

    /**
     * Whether the creature that was just dealt damage was a blocking creature. For combat damage the
     * damage source answers it: a creature deals combat damage to creatures blocking it only while it
     * is attacking (a blocking creature damages the attacker instead). The damaged creature's own
     * blocking flag is unusable there — lethal damage may already have removed it from the
     * battlefield, and combat state is torn down when the creature it blocked dies.
     */
    private boolean wasBlocking(GameData gameData, Permanent damageSource, UUID damagedCreatureId, boolean combatDamage) {
        if (combatDamage) return damageSource.isAttacking();
        Permanent damaged = gameQueryService.findPermanentById(gameData, damagedCreatureId);
        return damaged != null && damaged.isBlocking();
    }

    // ── Any-creature-dealt-damage triggers ─────────────────────────────

    /**
     * Fires ON_ANY_CREATURE_DEALT_DAMAGE triggers on every permanent with that slot, regardless of
     * who controls the damaged creature. Called once per damaged creature.
     */
    public void checkAnyCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature,
                                                    int damageDealt) {
        UUID damagedCreatureControllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        checkAnyCreatureDealtDamageTriggers(gameData, damagedCreature, damagedCreatureControllerId, damageDealt);
    }

    /**
     * Variant for combat, where the damaged creature may already have left the battlefield by the
     * time its trigger is queued and its controller was captured during damage processing.
     */
    public void checkAnyCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature,
                                                    UUID damagedCreatureControllerId, int damageDealt) {
        if (damagedCreatureControllerId == null || damageDealt <= 0) return;

        var ctx = new TriggerContext.AnyCreatureDealtDamage(
                damagedCreature, damagedCreatureControllerId, damageDealt);
        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = DamageWatch.ON_ANY_CREATURE_DEALT_DAMAGE.effectsOf(LayerSystemService.currentCard(gameData, perm));
            if (effects == null || effects.isEmpty()) return;
            if (gameQueryService.hasLostAllAbilities(gameData, perm)) return;

            for (CardEffect effect : effects) {
                var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                dispatch(match, DamageWatch.ON_ANY_CREATURE_DEALT_DAMAGE, effect, ctx);
            }
        });

        collectTemporaryGlobalTriggers(gameData, DamageWatch.ON_ANY_CREATURE_DEALT_DAMAGE,
                damagedCreature.getId(), damageDealt);

        for (DelayedWatchedCreatureDealtDamage watch
                : gameData.getDelayedActions(DelayedWatchedCreatureDealtDamage.class)) {
            if (!watch.watchedPermanentId().equals(damagedCreature.getId())) {
                continue;
            }

            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watch.sourceCard(),
                    watch.controllerId(),
                    watch.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(watch.effects()),
                    (UUID) null,
                    watch.watchedPermanentId());
            trigger.setNonTargeting(true);
            trigger.setEventValue(damageDealt);
            trigger.setDamageSourceCard(damagedCreature.getCard());
            trigger.setDamageSourceControllerId(
                    gameQueryService.findPermanentController(gameData, damagedCreature.getId()));
            trigger.setSourcePermanentSnapshot(new Permanent(damagedCreature));
            gameData.enqueueTrigger(trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
            log.info("Game {} - {} delayed trigger fires after damage to watched creature",
                    gameData.id, watch.sourceCard().getName());
        }
    }

    /** Fires ON_ANY_PERMANENT_DEALT_DAMAGE triggers once for the damaged permanent. */
    public void checkAnyPermanentDealtDamageTriggers(GameData gameData, Permanent damagedPermanent,
                                                     int damageDealt) {
        if (damagedPermanent == null || damageDealt <= 0) return;

        UUID damagedPermanentControllerId = gameQueryService.findPermanentController(
                gameData, damagedPermanent.getId());
        if (damagedPermanentControllerId == null) return;

        var ctx = new TriggerContext.AnyPermanentDealtDamage(
                damagedPermanent, damagedPermanentControllerId, damageDealt);
        gameData.forEachPermanent((playerId, perm) ->
                dispatchWatch(gameData, perm, playerId, DamageWatch.ON_ANY_PERMANENT_DEALT_DAMAGE, ctx));
    }

    public void checkTemporaryGlobalCreatureAttackTriggers(GameData gameData, Permanent attacker) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(CombatWatch.ON_ANY_CREATURE_ATTACKS.trigger())) continue;
            CardEffect effect = watcher.effect();
            boolean matches = true;
            while (matches) {
                if (effect instanceof TriggeringPermanentControllerConditionalEffect conditional) {
                    if (!watcher.controllerId().equals(gameQueryService.findPermanentController(gameData, attacker.getId()))) {
                        matches = false;
                    } else {
                        effect = conditional.wrapped();
                    }
                } else if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    if (!predicateEvaluationService.matchesPermanentPredicate(gameData, attacker, conditional.predicate())) {
                        matches = false;
                    } else {
                        effect = conditional.wrapped();
                    }
                } else if (effect instanceof ConditionalEffect conditional
                        && conditional.condition() instanceof AttacksAlone) {
                    if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                            ConditionContext.forPermanent(attacker, watcher.controllerId()))) {
                        matches = false;
                    } else {
                        effect = conditional.wrapped();
                    }
                } else {
                    break;
                }
            }
            if (!matches) continue;
            CardEffect triggeredEffect = effect;
            if (triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                        watcher.sourceCard(), watcher.controllerId(),
                        new ArrayList<>(List.of(triggeredEffect)), null));
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
                continue;
            }
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, watcher.sourceCard(),
                    watcher.controllerId(), watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(triggeredEffect)), null, attacker.getId());
            entry.setTargetId(attacker.getId());
            entry.setSourcePermanentSnapshot(new Permanent(attacker));
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /** Fires temporary global triggers watching a creature attack the registering player's
     * battlefield, either directly or through one of that player's planeswalkers. A watcher bound to
     * a permanent ("attacks you or Tasha") only fires for attacks on the player or on that permanent. */
    public void checkTemporaryGlobalCreatureAttacksYouTriggers(GameData gameData, Permanent attacker,
                                                               UUID attackedPlayerId, UUID attackedTargetId) {
        if (attacker == null || attackedPlayerId == null) {
            return;
        }
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(CombatWatch.ON_CREATURE_ATTACKS_YOU.trigger())
                    || !watcher.controllerId().equals(attackedPlayerId)) {
                continue;
            }
            if (watcher.watchedPermanentId() != null
                    && !watcher.watchedPermanentId().equals(attackedTargetId)
                    && !attackedPlayerId.equals(attackedTargetId)) {
                continue;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    null,
                    attacker.getId());
            entry.setTargetId(attacker.getId());
            entry.setSourcePermanentSnapshot(new Permanent(attacker));
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary attacks-you trigger fires for {} attacking",
                    gameData.id, watcher.sourceCard().getName(), attacker.getCard().getName());
        }
    }

    /** Fires temporary triggers that watch one or more creatures controlled by their controller attack. */
    public void checkTemporaryGlobalAllyCreatureAttackTriggers(GameData gameData, UUID attackingPlayerId) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(CombatWatch.ON_ALLY_CREATURES_ATTACK.trigger())
                    || !watcher.controllerId().equals(attackingPlayerId)) {
                continue;
            }

            CardEffect triggeredEffect = watcher.effect();
            if (triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || triggeredEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                        watcher.sourceCard(), watcher.controllerId(),
                        new ArrayList<>(List.of(triggeredEffect)), null));
            } else {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.sourceCard(), watcher.controllerId(),
                        watcher.sourceCard().getName() + "'s ability",
                        new ArrayList<>(List.of(triggeredEffect)));
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary ally attack trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    public void checkBendingTriggers(GameData gameData, UUID bendingPlayerId, BendingType type) {
        if (bendingPlayerId == null || type == null) {
            return;
        }
        gameData.recordBending(bendingPlayerId, type);
        TriggerContext context = new TriggerContext.Bending(bendingPlayerId, type);
        List<Permanent> battlefield = gameData.playerBattlefields.get(bendingPlayerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchWatch(gameData, permanent, bendingPlayerId, ActionWatch.ON_CONTROLLER_BENDS, context);
        }
    }

    /** Records that {@code exertedCreatureId} was exerted this turn and fires its controller's exert triggers. */
    public void checkExertTriggers(GameData gameData, UUID exertingPlayerId, UUID exertedCreatureId) {
        if (exertingPlayerId == null || exertedCreatureId == null) {
            return;
        }
        gameData.permanentsExertedThisTurn.add(exertedCreatureId);
        List<Permanent> battlefield = gameData.playerBattlefields.get(exertingPlayerId);
        if (battlefield == null) {
            return;
        }
        TriggerContext context = new TriggerContext.Exert(exertingPlayerId, exertedCreatureId);
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchWatch(gameData, permanent, exertingPlayerId, ActionWatch.ON_CONTROLLER_EXERTS, context);
        }
    }

    /**
     * Fires effects that care about an attacking creature causing one of its own triggered
     * abilities to trigger. The ability is supplied as a snapshot so later stack changes cannot
     * make a copy refer to a different trigger from the same source card.
     */
    public void checkAttackingCreatureTriggeredAbilityTriggers(GameData gameData, Permanent attacker,
                                                                StackEntry triggeredAbility) {
        checkAttackingCreatureTriggeredAbilityTriggers(gameData, attacker, triggeredAbility, false, null);
    }

    public void checkEnlistmentTriggeredAbilityTriggers(GameData gameData, Permanent attacker,
                                                        StackEntry triggeredAbility) {
        checkEnlistmentTriggeredAbilityTriggers(gameData, attacker, triggeredAbility, null);
    }

    public void checkEnlistmentTriggeredAbilityTriggers(GameData gameData, Permanent attacker,
                                                        StackEntry triggeredAbility,
                                                        Permanent enlistedCreature) {
        checkAttackingCreatureTriggeredAbilityTriggers(gameData, attacker, triggeredAbility, true,
                enlistedCreature);
    }

    private void checkAttackingCreatureTriggeredAbilityTriggers(GameData gameData, Permanent attacker,
                                                                StackEntry triggeredAbility, boolean enlistment,
                                                                Permanent enlistedCreature) {
        if (attacker == null || triggeredAbility == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
        if (controllerId == null) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.AttackingCreatureTriggeredAbility(
                attacker, triggeredAbility, enlistment, enlistedCreature);
        for (Permanent watcher : List.copyOf(battlefield)) {
            dispatchWatch(gameData, watcher, controllerId, CombatWatch.ON_ALLY_ATTACKING_CREATURE_ABILITY_TRIGGERS, context);
        }
    }

    /**
     * Collects turn-scoped global triggers for an event that identifies a permanent. {@code key} is the
     * {@code EffectSlot} or {@link TriggerWatch} the watchers were registered for.
     */
    public void collectTemporaryGlobalTriggers(GameData gameData, Object key, UUID targetId,
                                               int eventValue) {
        collectTemporaryGlobalTriggers(gameData, key, targetId, eventValue, null);
    }

    /** Collects turn-scoped global triggers while applying controller-relative permanent scopes. */
    public void collectTemporaryGlobalTriggers(GameData gameData, Object slot, UUID targetId,
                                               int eventValue, UUID targetControllerId) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watches(watcher, slot) || watcher.watchedPermanentId() != null
                    || (slot == CombatWatch.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER
                    && !watcher.controllerId().equals(
                    gameQueryService.findPermanentController(gameData, targetId)))) {
                continue;
            }

            if (slot == TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED
                    && (targetControllerId == null || !watcher.controllerId().equals(targetControllerId))) {
                continue;
            }
            if (slot == TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED
                    && (targetControllerId == null || watcher.controllerId().equals(targetControllerId))) {
                continue;
            }

            CardEffect resolvedEffect = watcher.effect();
            if (targetId != null && (slot == CombatWatch.ON_ANY_CREATURE_BLOCKS
                    || slot == TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED
                    || slot == TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED)
                    && resolvedEffect instanceof TriggeringPermanentConditionalEffect conditional) {
                Permanent triggeringPermanent = gameQueryService.findPermanentById(gameData, targetId);
                if (triggeringPermanent == null
                        || (conditional.predicate() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        triggeringPermanent,
                        conditional.predicate(),
                        FilterContext.of(gameData)
                                .withSourceCardId(watcher.sourceCard().getId())
                                .withSourceControllerId(watcher.controllerId())))) {
                    continue;
                }
                resolvedEffect = conditional.wrapped();
            }

            enqueueTemporaryGlobalTrigger(gameData, watcher, resolvedEffect, targetId, eventValue);
        }
    }

    /** Whether {@code watcher} was registered for {@code key}, an {@code EffectSlot} or a {@link TriggerWatch}. */
    private static boolean watches(TemporaryGlobalTriggeredAbility watcher, Object key) {
        return key instanceof TriggerWatch watch
                ? watcher.ability().hasTrigger(watch.trigger())
                : watcher.slot() == key;
    }

    private void enqueueTemporaryGlobalTrigger(GameData gameData,
                                                TemporaryGlobalTriggeredAbility watcher,
                                                UUID targetId, int eventValue) {
        enqueueTemporaryGlobalTrigger(gameData, watcher, watcher.effect(), targetId, eventValue);
    }

    private void enqueueTemporaryGlobalTrigger(GameData gameData,
                                                TemporaryGlobalTriggeredAbility watcher,
                                                CardEffect effect,
                                                UUID targetId, int eventValue) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.sourceCard(),
                watcher.controllerId(),
                watcher.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)));
        entry.setTargetId(targetId);
        entry.setEventValue(eventValue);
        if (watcher.ability().hasTrigger(TapWatch.ON_ANY_CREATURE_BECOMES_TAPPED.trigger())) {
            entry.setTriggeringPermanentId(targetId);
        }
        entry.setNonTargeting(true);
        if (watcher.ability().hasTrigger(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.trigger())
                || watcher.ability().hasTrigger(TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED.trigger())) {
            entry.setTriggeringPermanentId(targetId);
        }
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        log.info("Game {} - {} temporary global {} trigger fires",
                gameData.id, watcher.sourceCard().getName(), LegacySlots.label(watcher.ability()));
    }

    private void enqueueTemporaryGlobalTrigger(GameData gameData,
                                                TemporaryGlobalTriggeredAbility watcher,
                                                List<CardEffect> effects, UUID targetPlayerId) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.sourceCard(),
                watcher.controllerId(),
                watcher.sourceCard().getName() + "'s ability",
                new ArrayList<>(effects));
        entry.setTargetId(targetPlayerId);
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        log.info("Game {} - {} temporary global {} trigger fires",
                gameData.id, watcher.sourceCard().getName(), LegacySlots.label(watcher.ability()));
    }

    // ── Enchanted-permanent-tap triggers ───────────────────────────────

    public void beginPermanentTapTriggerBatch(GameData gameData) {
        if (gameData.permanentTapTriggerBatchDepth++ == 0) {
            gameData.permanentTapTriggerBatchFiredEffects.clear();
        }
    }

    public void endPermanentTapTriggerBatch(GameData gameData) {
        if (gameData.permanentTapTriggerBatchDepth <= 0) {
            throw new IllegalStateException("No permanent tap trigger batch is active");
        }
        if (--gameData.permanentTapTriggerBatchDepth == 0) {
            gameData.permanentTapTriggerBatchFiredEffects.clear();
        }
    }

    public void checkEnchantedPermanentTapTriggers(GameData gameData, Permanent tappedPermanent) {
        UUID tappingPlayerId = gameData.currentlyResolvingControllerId;
        if (tappingPlayerId == null && gameData.pendingEffectResolutionEntry != null) {
            tappingPlayerId = gameData.pendingEffectResolutionEntry.getControllerId();
        }
        checkEnchantedPermanentTapTriggers(gameData, tappedPermanent, tappingPlayerId);
    }

    public void checkEnchantedPermanentTapTriggers(GameData gameData, Permanent tappedPermanent,
                                                   UUID tappingPlayerId) {
        UUID tappedPermanentControllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(tappedPermanent)) {
                tappedPermanentControllerId = pid;
                break;
            }
        }
        if (tappedPermanentControllerId == null) return;

        final int creatureTapCount = gameQueryService.isCreature(gameData, tappedPermanent)
                ? gameData.creatureTapCountsThisTurn.merge(tappedPermanent.getId(), 1, Integer::sum)
                : 0;

        if (creatureTapCount > 0) {
            collectTemporaryGlobalTriggers(gameData, TapWatch.ON_ANY_CREATURE_BECOMES_TAPPED,
                    tappedPermanent.getId(), 0);
        }

        var ctx = new TriggerContext.EnchantedPermanentTap(tappedPermanent, tappedPermanentControllerId);

        collectEmblemArtifactTapTriggers(gameData, tappedPermanent, tappedPermanentControllerId, ctx);

        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(tappedPermanent.getId())) {
                return;
            }
            for (CardEffect effect : TapWatch.ON_ENCHANTED_PERMANENT_TAPPED.effectsOf(perm.getCard())) {
                var match = new TriggerMatchContext(gameData, perm, auraOwnerId, effect);
                dispatch(match, TapWatch.ON_ENCHANTED_PERMANENT_TAPPED, effect, ctx);
            }
        });

        // "Whenever a permanent you control becomes tapped" triggers (e.g. Judge of Currents).
        UUID controllerId = tappedPermanentControllerId;
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(controllerId)) return;
            List<EffectRegistration> printed = TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.registrationsOf(
                    LayerSystemService.currentCard(gameData, perm));
            List<EffectRegistration> registrations = printed.isEmpty()
                    || gameQueryService.hasLostPrintedAbilities(gameData, perm)
                    ? new ArrayList<>()
                    : new ArrayList<>(printed);
            perm.getTemporaryTriggerEffects(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.trigger()).stream()
                    .map(EffectRegistration::new).forEach(registrations::add);
            perm.getPersistentTriggerEffects(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.trigger()).stream()
                    .map(EffectRegistration::new).forEach(registrations::add);
            grantedTriggeredAbilitySupport.grantedTriggerEffects(
                            gameData, perm, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.trigger()).stream()
                    .map(EffectRegistration::new).forEach(registrations::add);
            for (EffectRegistration registration : registrations) {
                CardEffect effect = registration.effect();
                if (shouldSkipBatchedTapTrigger(gameData, perm, registration)) continue;
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(tappedPermanent, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                if (resolved instanceof OncePerTurnPerCreatureTriggerEffect oncePerCreature
                        && oncePerCreature.global()) {
                    if (!controllerId.equals(gameData.activePlayerId) || creatureTapCount != 1) {
                        continue;
                    }
                    resolved = oncePerCreature.wrapped();
                }
                if (resolved instanceof ChooseOneAtTriggerTimeEffect atTriggerTime
                        && atTriggerTime.maximumChoices() == null) {
                    gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                            perm.getCard(), ownerId, atTriggerTime.choice(), perm.getId(),
                            false, false, null, null, tappedPermanent.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers on ally permanent tap (awaiting mode)",
                            gameData.id, perm.getCard().getName());
                    markBatchedTapTrigger(gameData, perm, registration);
                    continue;
                }
                if (resolved.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved))));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers on ally permanent tap (awaiting graveyard target)",
                            gameData.id, perm.getCard().getName());
                    markBatchedTapTrigger(gameData, perm, registration);
                    continue;
                }
                // Bake the tapped permanent's controller only when damage needs
                // TRIGGERING_PERMANENT_CONTROLLER (Royal Decree). triggeringPermanentId always
                // carries "it" (Freyalise's Winds).
                boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect;
                resolved = unwrapOncePerTurnTrigger(gameData, perm, resolved);
                if (resolved == null) continue;
                if (resolved instanceof ConditionalEffect conditional && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(perm, ownerId))) {
                    continue;
                }
                // The target is chosen as the trigger goes on the stack (CR 603.3d), including for a
                // "you may pay" wrapper (Surgespanner). Bake the tapped permanent's controller only
                // when damage needs TRIGGERING_PERMANENT_CONTROLLER (Royal Decree).
                // triggeringPermanentId always carries "it" (Freyalise's Winds).
                if ((resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || resolved.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
                    gameData.queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved)), perm.getId(),
                            tappedPermanent.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers on ally permanent tap ({}, awaiting target)",
                            gameData.id, perm.getCard().getName(), tappedPermanent.getCard().getName());
                    if (oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                    }
                    markBatchedTapTrigger(gameData, perm, registration);
                    continue;
                }
                UUID bakedTargetId = bakeTriggeringPermanentControllerTarget(resolved, controllerId);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        bakedTargetId,
                        perm.getId()
                );
                entry.setTriggeringPermanentId(tappedPermanent.getId());
                if (resolved instanceof com.github.laxika.magicalvibes.model.effect
                        .BoostAndGrantKeywordToTriggeringAndSharingCreaturesUntilEndOfTurnEffect) {
                    entry.setAttachedPermanentSnapshot(new Permanent(tappedPermanent));
                }
                gameData.enqueueTrigger(entry);
                if (oncePerTurn) {
                    gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                }
                markBatchedTapTrigger(gameData, perm, registration);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ally permanent tap ({})",
                        gameData.id, perm.getCard().getName(), tappedPermanent.getCard().getName());
            }
        });

        // "Whenever a permanent an opponent controls becomes tapped" triggers (e.g. Thoughtleech).
        gameData.forEachPermanent((ownerId, perm) -> {
            if (ownerId.equals(controllerId)) return;
            for (EffectRegistration registration
                    : TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED.registrationsOf(perm.getCard())) {
                if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) break;
                CardEffect effect = registration.effect();
                if (shouldSkipBatchedTapTrigger(gameData, perm, registration)) continue;
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(tappedPermanent, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect;
                resolved = unwrapOncePerTurnTrigger(gameData, perm, resolved);
                if (resolved == null) continue;
                  if (resolved instanceof ConditionalEffect conditional && conditional.interveningIf()
                          && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                          ConditionContext.forPermanent(perm, ownerId))) {
                      continue;
                  }
                if (resolved.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved))));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} triggers on opponent permanent tap (awaiting graveyard target)",
                            gameData.id, perm.getCard().getName());
                    markBatchedTapTrigger(gameData, perm, registration);
                    continue;
                }
                UUID bakedTargetId = bakeTriggeringPermanentControllerTarget(resolved, controllerId);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        bakedTargetId,
                        perm.getId()
                );
                entry.setTriggeringPermanentId(tappedPermanent.getId());
                gameData.enqueueTrigger(entry);
                if (oncePerTurn) {
                    gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                }
                markBatchedTapTrigger(gameData, perm, registration);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on opponent permanent tap ({})",
                        gameData.id, perm.getCard().getName(), tappedPermanent.getCard().getName());
            }
        });

        collectTemporaryGlobalTriggers(gameData, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                tappedPermanent.getId(), 0, controllerId);
        collectTemporaryGlobalTriggers(gameData, TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED,
                tappedPermanent.getId(), 0, controllerId);

        // "Whenever you tap an untapped creature an opponent controls" triggers. The tapping
        // player is tracked separately from the tapped permanent's controller because an effect
        // may instruct another player to tap their own permanent.
        if (tappingPlayerId == null || tappingPlayerId.equals(controllerId)) {
            return;
        }
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(tappingPlayerId)) return;
            if (gameQueryService.hasLostAllAbilities(gameData, perm)) return;
            List<EffectRegistration> registrations = new ArrayList<>(
                    TapWatch.ON_CONTROLLER_TAPS_OPPONENT_PERMANENT.registrationsOf(perm.getCard()));
            perm.getTemporaryTriggerEffects(TapWatch.ON_CONTROLLER_TAPS_OPPONENT_PERMANENT.trigger()).stream()
                    .map(EffectRegistration::new).forEach(registrations::add);
            perm.getPersistentTriggerEffects(TapWatch.ON_CONTROLLER_TAPS_OPPONENT_PERMANENT.trigger()).stream()
                    .map(EffectRegistration::new).forEach(registrations::add);
            grantedTriggeredAbilitySupport.grantedTriggerEffects(
                            gameData, perm, TapWatch.ON_CONTROLLER_TAPS_OPPONENT_PERMANENT.trigger()).stream()
                    .map(EffectRegistration::new).forEach(registrations::add);
            for (EffectRegistration registration : registrations) {
                CardEffect effect = registration.effect();
                if (shouldSkipBatchedTapTrigger(gameData, perm, registration)) continue;
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(tappedPermanent, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect;
                resolved = unwrapOncePerTurnTrigger(gameData, perm, resolved);
                if (resolved == null) continue;
                if (resolved instanceof ConditionalEffect conditional && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(perm, ownerId))) {
                    continue;
                }
                if (resolved.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved))));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    markBatchedTapTrigger(gameData, perm, registration);
                    continue;
                }
                if (resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        && !(resolved instanceof MayPayManaEffect)) {
                    gameData.queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved)), perm.getId(),
                            tappedPermanent.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    if (oncePerTurn) {
                        gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                    }
                    markBatchedTapTrigger(gameData, perm, registration);
                    continue;
                }
                UUID bakedTargetId = bakeTriggeringPermanentControllerTarget(resolved, controllerId);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        bakedTargetId,
                        perm.getId()
                );
                entry.setTriggeringPermanentId(tappedPermanent.getId());
                gameData.enqueueTrigger(entry);
                if (oncePerTurn) {
                    gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                }
                markBatchedTapTrigger(gameData, perm, registration);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            }
        });
    }

    private void collectEmblemArtifactTapTriggers(GameData gameData, Permanent tappedPermanent,
                                                   UUID tappedPermanentControllerId,
                                                   TriggerContext.EnchantedPermanentTap context) {
        if (!gameQueryService.isArtifact(gameData, tappedPermanent)) {
            return;
        }
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(tappedPermanentControllerId)) {
                continue;
            }
            for (CardEffect effect : emblem.staticEffects()) {
                if (!(effect instanceof EmblemArtifactTapTriggerEffect)) {
                    continue;
                }
                var match = new EmblemTriggerMatchContext(
                        gameData, emblem, emblem.controllerId(), tappedPermanent.getCard(), effect);
                registry.dispatchEmblem(match, effect, context);
            }
        }
    }

    @CollectsEmblemTrigger(EmblemArtifactTapTriggerEffect.class)
    boolean handleEmblemArtifactTap(EmblemTriggerMatchContext match,
                                    EmblemArtifactTapTriggerEffect effect,
                                    TriggerContext context) {
        Card source = match.emblem().sourceCard();
        if (source == null) {
            return false;
        }
        String description = source.getName() + "'s emblem";
        match.gameData().enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source,
                match.controllerId(),
                description,
                new ArrayList<>(List.of(new DrawCardEffect(1)))
        ));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(source));
        return true;
    }

    private static boolean shouldSkipBatchedTapTrigger(GameData gameData, Permanent source,
                                                        EffectRegistration registration) {
        return registration.triggerMode() == TriggerMode.ONCE_PER_BATCH
                && gameData.permanentTapTriggerBatchDepth > 0
                && gameData.permanentTapTriggerBatchFiredEffects.getOrDefault(source.getId(), Set.of())
                .contains(registration.effect());
    }

    private static void markBatchedTapTrigger(GameData gameData, Permanent source,
                                               EffectRegistration registration) {
        if (registration.triggerMode() != TriggerMode.ONCE_PER_BATCH
                || gameData.permanentTapTriggerBatchDepth <= 0) {
            return;
        }
        gameData.permanentTapTriggerBatchFiredEffects
                .computeIfAbsent(source.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(registration.effect());
    }

    /**
     * Collects abilities that trigger when a creature pays a Vehicle's crew cost. The Vehicle is
     * carried as the triggering permanent so the triggered effect can resolve against that object.
     */
    public void checkCrewsVehicleTriggers(GameData gameData, Permanent crewingCreature, Permanent vehicle) {
        if (vehicle == null || !vehicle.getCard().getSubtypes().contains(CardSubtype.VEHICLE)) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, crewingCreature.getId());
        if (controllerId == null) {
            return;
        }
        for (CardEffect effect : MiscWatch.ON_CREWS_VEHICLE.effectsOf(crewingCreature.getCard())) {
            if (effect instanceof com.github.laxika.magicalvibes.model.effect.RegisterDelayedVehicleAttackEffect registration) {
                if (!gameData.hasDelayedAction(com.github.laxika.magicalvibes.model.action.DelayedVehicleAttack.class,
                        action -> action.sourcePermanentId().equals(crewingCreature.getId())
                                && action.vehicleId().equals(vehicle.getId()))) {
                    gameData.queueDelayedAction(new com.github.laxika.magicalvibes.model.action.DelayedVehicleAttack(
                            controllerId, crewingCreature.getId(), vehicle.getId(), crewingCreature.getCard(),
                            registration.effect()));
                }
                continue;
            }
            if (gameQueryService.hasLostPrintedAbilities(gameData, crewingCreature)) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    crewingCreature.getCard(),
                    controllerId,
                    crewingCreature.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    crewingCreature.getId()
            );
            entry.setTriggeringPermanentId(vehicle.getId());
            entry.setSourcePermanentSnapshot(new Permanent(crewingCreature));
            gameData.pendingActivatedAbilityCostTriggers.add(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(crewingCreature.getCard()));
            log.info("Game {} - {} triggers when it crews {}",
                    gameData.id, crewingCreature.getCard().getName(), vehicle.getCard().getName());
        }
    }

    /** Collects abilities watching a creature controlled by a player crew a Vehicle. */
    public void checkAllyCreatureCrewsVehicleTriggers(GameData gameData, Permanent crewingCreature,
                                                       Permanent vehicle) {
        if (vehicle == null || !vehicle.getCard().getSubtypes().contains(CardSubtype.VEHICLE)) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, crewingCreature.getId());
        if (controllerId == null) {
            return;
        }
        TriggerContext context = new TriggerContext.CreatureCrewsVehicle(crewingCreature, vehicle);
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent watcher : List.copyOf(battlefield)) {
            dispatchWatch(gameData, watcher, controllerId,
                    MiscWatch.ON_ALLY_CREATURE_CREWS_VEHICLE, context);
        }
    }

    /** Collects abilities that trigger when a creature is tapped to pay a teamwork cost. */
    public void checkSelfTappedToPayTeamworkCostTriggers(GameData gameData, Permanent tappedCreature) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, tappedCreature.getId());
        if (controllerId == null || tappedCreature.isLosesAllAbilitiesUntilEndOfTurn()) {
            return;
        }
        List<CardEffect> effects = new ArrayList<>(
                TapWatch.ON_SELF_TAPPED_TO_PAY_TEAMWORK_COST.effectsOf(tappedCreature.getCard()));
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                gameData, tappedCreature, TapWatch.ON_SELF_TAPPED_TO_PAY_TEAMWORK_COST.trigger()));
        if (effects.isEmpty()) {
            return;
        }
        int triggerCopies = 1 + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                gameData, controllerId, tappedCreature);
        for (CardEffect effect : effects) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    tappedCreature.getCard(),
                    controllerId,
                    tappedCreature.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    tappedCreature.getId());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            for (int copy = 1; copy < triggerCopies; copy++) {
                gameData.enqueueTrigger(entry.copyForNewStackObject());
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(tappedCreature.getCard()));
            log.info("Game {} - {} triggers when tapped for teamwork",
                    gameData.id, tappedCreature.getCard().getName());
        }
    }

    /**
     * For ally/opponent becomes-tapped slots: bake the tapped permanent's controller as
     * {@code targetId} only when the resolved effect deals damage to
     * {@link DamageRecipient#TRIGGERING_PERMANENT_CONTROLLER}. Other effects keep {@code null}
     * so may-target tap triggers (Surgespanner) still choose at resolution.
     */
    private static UUID bakeTriggeringPermanentControllerTarget(CardEffect resolved, UUID tappedControllerId) {
        if (resolved instanceof DealDamageToPlayersEffect damage
                && damage.recipient() == DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER) {
            return tappedControllerId;
        }
        return null;
    }

    // ── Becomes-untapped triggers ──────────────────────────────────────

    /**
     * "Whenever this permanent becomes untapped" triggers (e.g. Hollowsage). Called from the untap
     * call sites after a permanent transitions from tapped to untapped. Fires only on the permanent
     * that became untapped, queueing its triggered ability with the permanent as its source;
     * targeted effects choose targets as the ability is put on the stack.
     */
    public void checkBecomesUntappedTriggers(GameData gameData, Permanent untappedPermanent) {
        UUID controllerId = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null && bf.contains(untappedPermanent)) {
                controllerId = pid;
                break;
            }
        }
        if (controllerId == null) return;

        processDelayedDestroyTargetWhenSourceUntaps(gameData, untappedPermanent);

        boolean lostPrintedAbilities = gameQueryService.hasLostPrintedAbilities(gameData, untappedPermanent);
        for (CardEffect effect : TapWatch.ON_SELF_BECOMES_UNTAPPED.effectsOf(untappedPermanent.getCard())) {
            if (lostPrintedAbilities) break;
            if (effect instanceof com.github.laxika.magicalvibes.model.effect.PhaseInLinkedPermanentEffect
                    && gameData.phasedOutWhileSourceTapped
                    .getOrDefault(untappedPermanent.getId(), java.util.Set.of()).isEmpty()) {
                continue;
            }
            var match = new TriggerMatchContext(gameData, untappedPermanent, controllerId, effect);
            dispatch(match, TapWatch.ON_SELF_BECOMES_UNTAPPED, effect,
                    new TriggerContext.SelfBecomesUntapped(controllerId));
        }

        // Granted "Whenever this creature becomes untapped" abilities (Well Rested) have the enchanted
        // creature as their source and are controlled by its controller.
        for (CardEffect granted : grantedTriggeredAbilitySupport.grantedTriggerEffects(
                gameData, untappedPermanent, controllerId, TapWatch.ON_SELF_BECOMES_UNTAPPED.trigger())) {
            CardEffect resolved = OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, untappedPermanent, granted);
            if (resolved == null) continue;
            StackEntry grantedEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    untappedPermanent.getCard(),
                    controllerId,
                    untappedPermanent.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(resolved)),
                    null,
                    untappedPermanent.getId());
            if (granted instanceof OncePerTurnTriggerEffect once && once.markOnAcceptance()) {
                grantedEntry.setMarkSourceOncePerTurnOnAcceptance(true);
            }
            gameData.enqueueTrigger(grantedEntry);
            OncePerTurnTriggerSupport.markIfNeeded(gameData, untappedPermanent, granted);
            gameLogService.append(gameData, GameLog.abilityTriggers(untappedPermanent.getCard()));
            log.info("Game {} - {} triggers on becoming untapped (granted ability)",
                    gameData.id, untappedPermanent.getCard().getName());
        }

        // "Whenever a permanent you control becomes untapped" triggers (e.g. Wake Thrasher).
        UUID untappedControllerId = controllerId;
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(untappedControllerId)) return;
            for (CardEffect effect : TapWatch.ON_ALLY_PERMANENT_BECOMES_UNTAPPED.effectsOf(perm.getCard())) {
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(untappedPermanent, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ally permanent untap ({})",
                        gameData.id, perm.getCard().getName(), untappedPermanent.getCard().getName());
            }
        });

        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, perm)) return;
            for (CardEffect effect : TapWatch.ON_ANY_PERMANENT_BECOMES_UNTAPPED.effectsOf(perm.getCard())) {
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(
                            untappedPermanent, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                CardEffect triggerEffect = resolved;
                resolved = OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, perm, resolved);
                if (resolved == null) continue;
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                );
                entry.setEventPlayerIds(List.of(untappedControllerId));
                entry.setTriggeringPermanentId(untappedPermanent.getId());
                gameData.enqueueTrigger(entry);
                OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, triggerEffect);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on any permanent untap ({})",
                        gameData.id, perm.getCard().getName(), untappedPermanent.getCard().getName());
            }
        });
    }

    /**
     * Checks triggers that care about one or more permanents untapping during the active player's
     * untap step. The event is dispatched once with the total count.
     */
    public void checkControllerUntapsDuringUntapStepTriggers(GameData gameData, UUID controllerId,
                                                               int untappedPermanentCount) {
        if (untappedPermanentCount <= 0) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        TriggerContext context = new TriggerContext.UntapStep(untappedPermanentCount);
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchWatch(gameData, permanent, controllerId,
                    TapWatch.ON_CONTROLLER_UNTAPS_DURING_UNTAP_STEP, context);
        }
    }

    /**
     * "When this creature becomes renowned" (Relic Seeker) and "whenever a creature you control
     * becomes renowned" (Valeron Wardens) triggers. Called from
     * {@code RenownEffectHandler} at the moment renown flips the creature from not-renowned to
     * renowned; a creature that was already renowned never reaches this point (CR 702.112c).
     *
     * @param gameData         the current game state to modify
     * @param renownedCreature the creature that just became renowned
     * @param controllerId     the player controlling it
     */
    public void checkBecomesRenownedTriggers(GameData gameData, Permanent renownedCreature, UUID controllerId) {
        for (CardEffect effect : CounterWatch.ON_SELF_BECOMES_RENOWNED.effectsOf(renownedCreature.getCard())) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    renownedCreature.getCard(),
                    controllerId,
                    renownedCreature.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    renownedCreature.getId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(renownedCreature.getCard()));
            log.info("Game {} - {} triggers on becoming renowned", gameData.id, renownedCreature.getCard().getName());
        }

        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(controllerId)) return;
            for (CardEffect effect : CounterWatch.ON_ALLY_CREATURE_BECOMES_RENOWNED.effectsOf(perm.getCard())) {
                CardEffect resolved = effect;
                if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext filterContext = FilterContext.of(gameData)
                            .withSourceCardId(perm.getOriginalCard().getId())
                            .withSourceControllerId(ownerId);
                    if (!predicateEvaluationService.matchesPermanentPredicate(renownedCreature, conditional.predicate(), filterContext)) {
                        continue;
                    }
                    resolved = conditional.wrapped();
                }
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ally creature becoming renowned ({})",
                        gameData.id, perm.getCard().getName(), renownedCreature.getCard().getName());
            }
        });
    }

    /** Fires abilities that trigger whenever a player becomes the monarch. */
    public void checkBecomesMonarchTriggers(GameData gameData, UUID monarchPlayerId) {
        gameData.forEachPermanent((ownerId, permanent) -> {
            boolean controllerBecameMonarch = ownerId.equals(monarchPlayerId);
            ActionWatch watch = controllerBecameMonarch
                    ? ActionWatch.ON_CONTROLLER_BECOMES_MONARCH
                    : ActionWatch.ON_OPPONENT_BECOMES_MONARCH;
            for (CardEffect effect : watch.effectsOf(permanent.getCard())) {
                if (effect instanceof ConditionalEffect conditional
                        && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(permanent, ownerId))) {
                    continue;
                }

                List<CardEffect> effects = new ArrayList<>(List.of(effect));
                if (effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                    int targetGroupIndex = permanent.getCard().getEffectTargetIndex(effect);
                    TargetFilter targetFilter = targetGroupIndex >= 0
                            && targetGroupIndex < permanent.getCard().getTargetGroups().size()
                            ? permanent.getCard().getTargetGroups().get(targetGroupIndex).getFilter()
                            : permanent.getCard().getTargetFilter();
                    boolean playerTargetOnly = effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                            && !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT);
                    UUID targetControllerId = effect instanceof TargetControllerIsTriggeringPlayerEffect
                            ? monarchPlayerId : null;
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            permanent.getCard(), ownerId, effects, playerTargetOnly, targetFilter,
                            0, permanent.getId(), new Permanent(permanent), false, null,
                            targetControllerId, ownerId));
                } else {
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            permanent.getCard(),
                            ownerId,
                            permanent.getCard().getName() + "'s ability",
                            effects,
                            controllerBecameMonarch ? null : monarchPlayerId,
                            permanent.getId()
                    );
                    entry.setNonTargeting(true);
                    entry.setSourcePermanentSnapshot(new Permanent(permanent));
                    gameData.enqueueTrigger(entry);
                }
                gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                log.info("Game {} - {} triggers when {} becomes monarch", gameData.id,
                        permanent.getCard().getName(), controllerBecameMonarch ? "its controller" : "an opponent");
            }
        });
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellTargetTriggerAnyTarget.class)
                && !gameData.interaction.isAwaitingInput()) {
            processNextSpellTargetTrigger(gameData);
        }
    }

    /** Fires abilities that trigger whenever the controller solves a Case. */
    public void checkAllyCaseSolvesTriggers(GameData gameData, Permanent solvedCase, UUID controllerId) {
        gameData.forEachPermanent((ownerId, permanent) -> {
            if (!ownerId.equals(controllerId)) return;
            for (CardEffect effect : ActionWatch.ON_ALLY_CASE_SOLVES.effectsOf(permanent.getCard())) {
                CardEffect resolved = unwrapTriggeringCardConditional(effect, solvedCase.getCard(), gameData,
                        controllerId);
                if (resolved == null) continue;
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        permanent.getCard(),
                        controllerId,
                        permanent.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        permanent.getId()
                );
                entry.setTriggeringPermanentId(solvedCase.getId());
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                log.info("Game {} - {} triggers when a Case is solved", gameData.id,
                        permanent.getCard().getName());
            }
        });
    }

    /** Fires the newly monstrous permanent's own triggered abilities. */
    public void checkBecomesMonstrousTriggers(GameData gameData, Permanent monstrousPermanent,
                                               UUID controllerId) {
        checkBecomesMonstrousTriggers(gameData, monstrousPermanent, controllerId, 0);
    }

    public void checkBecomesMonstrousTriggers(GameData gameData, Permanent monstrousPermanent,
                                               UUID controllerId, int xValue) {
        TriggerContext.SelfBecomesMonstrous context =
                new TriggerContext.SelfBecomesMonstrous(controllerId, xValue);
        for (CardEffect effect : CounterWatch.ON_SELF_BECOMES_MONSTROUS.effectsOf(monstrousPermanent.getCard())) {
            var match = new TriggerMatchContext(gameData, monstrousPermanent, controllerId, effect);
            registry.dispatch(match, CounterWatch.ON_SELF_BECOMES_MONSTROUS, effect, context);
        }
    }

    /**
     * "When this phases out" and "when this phases in" (Teferi's Imp): each effect is its own ability. A targeted
     * phase-in ability (Shimmering Efreet) chooses its target as it is put on the stack.
     */
    private boolean enqueuePhasingTriggers(GameData gameData, Permanent permanent, UUID controllerId,
                                           List<CardEffect> effects, boolean phasesIn) {
        String direction = phasesIn ? "in" : "out";
        for (CardEffect effect : effects) {
            if (phasesIn && effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                gameData.queueInteraction(new PermanentChoiceContext.PhasesInTriggerTarget(
                        permanent.getCard(), controllerId, new ArrayList<>(List.of(effect)), permanent.getId()));
                gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                log.info("Game {} - {} triggers on phasing {} (awaiting target)",
                        gameData.id, permanent.getCard().getName(), direction);
                continue;
            }
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    permanent.getCard(),
                    controllerId,
                    permanent.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    permanent.getId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
            log.info("Game {} - {} triggers on phasing {}", gameData.id, permanent.getCard().getName(), direction);
        }
        return !effects.isEmpty();
    }

    // ── Ability-activation triggers ────────────────────────────────────

    /**
     * "Whenever you activate an ability of {a permanent}" triggers (e.g. Ceaseless Searblades).
     * Fires on every permanent the activating player controls that has an
     * {@link SpellAbilityWatch#ON_CONTROLLER_ACTIVATES_ABILITY} effect, filtered (when wrapped in
     * {@link TriggeringPermanentConditionalEffect}) by the permanent whose ability was activated.
     */
    public void checkControllerActivatesAbilityTriggers(GameData gameData, UUID activatingPlayerId,
                                                        Permanent activatedPermanent, ActivatedAbility ability,
                                                        UUID activatedPermanentControllerId,
                                                        List<CardEffect> preCollectedEffects) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(activatingPlayerId)) return;
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) return;
            for (CardEffect effect : SpellAbilityWatch.ON_CONTROLLER_ACTIVATES_ABILITY.effectsOf(perm.getCard())) {
                if (isPreCollectedActivationEffect(effect, preCollectedEffects)) continue;
                // Copy descriptors are collected after the activated ability has been put on the
                // stack so their snapshot contains the activation's targets and X value.
                if (effect instanceof CopyControllerActivatedAbilityTriggerEffect
                        || effect instanceof OncePerTurnTriggerEffect once
                        && once.wrapped() instanceof CopyControllerActivatedAbilityTriggerEffect) {
                    continue;
                }
                CardEffect authoredEffect = effect.resolveForActivatedAbility(ability);
                if (authoredEffect == null) continue;
                CardEffect resolved = OncePerTurnTriggerSupport.unwrapIfAvailable(
                        gameData, perm, authoredEffect);
                if (resolved == null) continue;
                resolved = resolveTreasureManaActivationConditional(gameData, activatedPermanent, resolved);
                if (resolved == null) continue;
                resolved = resolveTriggeringPermanentConditional(gameData, perm, ownerId, activatedPermanent, resolved);
                if (resolved == null) continue;
                if (resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        && !resolved.targetSpec().declares(TargetPredicates.anyTarget())) {
                    gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved)),
                            "ability activation", perm.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} queues a permanent-target ability-activation trigger ({})",
                            gameData.id, perm.getCard().getName(), activatedPermanent.getCard().getName());
                    OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, authoredEffect);
                    continue;
                }
                if (resolved.targetSpec().declares(TargetPredicates.anyTarget())) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved)), false, null, 0, perm.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} queues an any-target ability-activation trigger ({})",
                            gameData.id, perm.getCard().getName(), activatedPermanent.getCard().getName());
                    OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, authoredEffect);
                    continue;
                }
                UUID targetId = activationTriggerTargetId(
                        resolved, null, null, activatedPermanentControllerId);
                int triggerCopies = 1 + gameQueryService.countAdditionalTriggeredAbilityTriggers(
                        gameData, ownerId, perm);
                for (int copy = 0; copy < triggerCopies; copy++) {
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            ownerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(resolved)),
                            targetId,
                            perm.getId()
                    );
                    if (targetId != null) {
                        entry.setNonTargeting(true);
                    }
                    if (authoredEffect instanceof OncePerTurnTriggerEffect once && once.markOnAcceptance()) {
                        entry.setMarkSourceOncePerTurnOnAcceptance(true);
                    }
                    entry.setTriggeringPermanentId(activatedPermanent.getId());
                    entry.setTriggeringPermanentControllerId(activatedPermanentControllerId);
                    gameData.enqueueTrigger(entry);
                }
                OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, authoredEffect);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ability activation ({})",
                        gameData.id, perm.getCard().getName(), activatedPermanent.getCard().getName());
            }
        });
    }

    /**
     * A single "when you next cast a spell with {X} in its mana cost or activate an ability with {X} in
     * its activation cost" delayed trigger (Magus Lucea Kane) is registered as one spell copy and one
     * ability copy; when the ability half fires, the spell half is used up with it.
     */
    private void consumeLinkedXSpellCopies(GameData gameData, UUID playerId, int count) {
        List<CopyNextSpellCastThisTurnEffect> pending = gameData.pendingNextFilteredSpellCopiesThisTurn.get(playerId);
        if (pending == null) {
            return;
        }
        List<CopyNextSpellCastThisTurnEffect> remaining = new ArrayList<>(pending);
        for (int i = 0; i < count; i++) {
            CopyNextSpellCastThisTurnEffect linked = remaining.stream()
                    .filter(copyEffect -> copyEffect.spellFilter() instanceof CardHasXInManaCostPredicate)
                    .findFirst().orElse(null);
            if (linked == null) {
                break;
            }
            remaining.remove(linked);
        }
        if (remaining.isEmpty()) {
            gameData.pendingNextFilteredSpellCopiesThisTurn.remove(playerId);
        } else {
            gameData.pendingNextFilteredSpellCopiesThisTurn.put(playerId, remaining);
        }
    }

    /** The spell half of a linked X delayed trigger used up its ability half; see {@link #consumeLinkedXSpellCopies}. */
    private void consumeLinkedXAbilityCopies(GameData gameData, UUID playerId, int count) {
        gameData.pendingNextXActivatedAbilityCopyThisTurnCount.computeIfPresent(playerId,
                (id, pendingCount) -> pendingCount - count > 0 ? pendingCount - count : null);
    }

    /**
     * Collects copy descriptors placed in {@link SpellAbilityWatch#ON_CONTROLLER_ACTIVATES_ABILITY}.
     * This runs with the completed stack entry so the copied ability retains its chosen targets and
     * X value. Mana abilities use a synthetic entry because they resolve without using the stack.
     */
    public void checkControllerActivatesAbilityCopyTriggers(GameData gameData, UUID activatingPlayerId,
                                                             StackEntry abilityEntry, ActivatedAbility ability,
                                                             Permanent activatedPermanent,
                                                             List<CardEffect> preCollectedEffects) {
        if (abilityEntry == null) return;

        Integer pendingXCopies = gameData.pendingNextXActivatedAbilityCopyThisTurnCount.get(activatingPlayerId);
        if (pendingXCopies != null && pendingXCopies > 0
                && ability.getManaCost() != null && new ManaCost(ability.getManaCost()).hasX()) {
            StackEntry snapshot = new StackEntry(abilityEntry);
            List<CardEffect> copyEffects = new ArrayList<>(pendingXCopies);
            for (int i = 0; i < pendingXCopies; i++) {
                copyEffects.add(new CopyControllerActivatedAbilityEffect(snapshot, ability, activatingPlayerId));
            }
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    abilityEntry.getCard(),
                    activatingPlayerId,
                    "Copy " + abilityEntry.getCard().getName() + "'s ability",
                    copyEffects
            ));
            gameData.pendingNextXActivatedAbilityCopyThisTurnCount.remove(activatingPlayerId);
            consumeLinkedXSpellCopies(gameData, activatingPlayerId, pendingXCopies);
            gameLogService.append(gameData, GameLog.cardThen(abilityEntry.getCard(), "'s ability is copied."));
            log.info("Game {} - {} delayed X-ability copy trigger(s) queued for {}",
                    gameData.id, pendingXCopies, abilityEntry.getCard().getName());
        }

        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(activatingPlayerId)) return;
            for (CardEffect effect : SpellAbilityWatch.ON_CONTROLLER_ACTIVATES_ABILITY.effectsOf(perm.getCard())) {
                if (isPreCollectedActivationEffect(effect, preCollectedEffects)) {
                    continue;
                }
                CardEffect authoredEffect = effect;
                CardEffect availableEffect = OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, perm, effect);
                if (!(availableEffect instanceof CopyControllerActivatedAbilityTriggerEffect trigger)) {
                    continue;
                }
                if (trigger.sourceFilter() != null && !targetLegalityService.matchesStackEntryPredicate(
                        gameData, abilityEntry, trigger.sourceFilter(), ownerId, perm)) {
                    continue;
                }
                if (trigger.sourcePermanentFilter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        activatedPermanent,
                        trigger.sourcePermanentFilter(),
                        FilterContext.of(gameData)
                                .withSourceControllerId(ownerId)
                                .withSourcePermanentSnapshot(perm))) {
                    continue;
                }
                if (trigger.targetPredicate() != null && !targetLegalityService.matchesStackEntryPredicate(
                        gameData, abilityEntry, trigger.targetPredicate(), ownerId, perm)) {
                    continue;
                }
                if (trigger.activationCostContainsX()
                        && (ability.getManaCost() == null || !new ManaCost(ability.getManaCost()).hasX())) {
                    continue;
                }
                if (trigger.equippedCreatureOnly()
                        && (perm.getAttachedTo() == null
                        || !perm.getAttachedTo().equals(abilityEntry.getSourcePermanentId()))) {
                    continue;
                }

                UUID copyControllerId = trigger.equippedCreatureOnly() ? ownerId : activatingPlayerId;
                CardEffect copyEffect = new CopyControllerActivatedAbilityEffect(
                        new StackEntry(abilityEntry), ability, copyControllerId);
                if (trigger.manaCost() != null) {
                    copyEffect = new MayPayManaEffect(
                            trigger.manaCost(),
                            copyEffect,
                            "Pay " + trigger.manaCost() + " to copy " + abilityEntry.getCard().getName() + "'s ability?");
                }

                StackEntry triggerEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(copyEffect)),
                        null,
                        perm.getId()
                );
                if (authoredEffect instanceof OncePerTurnTriggerEffect once && once.markOnAcceptance()) {
                    triggerEntry.setMarkSourceOncePerTurnOnAcceptance(true);
                }
                gameData.enqueueTrigger(triggerEntry);
                OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, authoredEffect);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ability activation ({})",
                        gameData.id, perm.getCard().getName(), abilityEntry.getCard().getName());
            }
        });
    }

    /** Fires battlefield triggers that specifically watch hand ninjutsu activations. */
    public void checkControllerActivatesNinjutsuAbilityTriggers(GameData gameData, UUID activatingPlayerId,
                                                                 ActivatedAbility ability) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(activatingPlayerId)) return;
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) return;
            for (CardEffect effect : SpellAbilityWatch.ON_CONTROLLER_ACTIVATES_ABILITY.effectsOf(perm.getCard())) {
                if (!(effect instanceof TriggeringNinjutsuAbilityConditionalEffect)) continue;
                CardEffect resolved = effect.resolveForActivatedAbility(ability);
                if (resolved == null) continue;
                boolean oncePerTurn = resolved instanceof OncePerTurnTriggerEffect;
                resolved = OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, perm, resolved);
                if (resolved == null) continue;
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                ));
                if (oncePerTurn) {
                    OncePerTurnTriggerSupport.markIfNeeded(gameData, perm,
                            effect.resolveForActivatedAbility(ability));
                }
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on ninjutsu activation", gameData.id, perm.getCard().getName());
            }
        });
    }

    /**
     * Fires "whenever you activate an exhaust ability" triggers for the activating player's
     * permanents. The caller invokes this only after the exhaust ability's activation costs have
     * been paid, so the trigger is queued above the activated ability.
     */
    public void checkControllerActivatesExhaustAbilityTriggers(GameData gameData, UUID activatingPlayerId) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(activatingPlayerId)) return;
            for (CardEffect effect : SpellAbilityWatch.ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY.effectsOf(perm.getCard())) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on exhaust ability activation", gameData.id, perm.getCard().getName());
            }
        });
    }

    /**
     * Fires graveyard-resident "whenever you activate an exhaust ability" triggers for the
     * activating player. These abilities function from the graveyard and return their own card.
     */
    public void checkControllerActivatesExhaustAbilityTriggersFromGraveyard(
            GameData gameData, UUID activatingPlayerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(activatingPlayerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            for (CardEffect effect : gameQueryService.getEffectiveGraveyardLegacyTriggerEffects(
                    gameData, card, SpellAbilityWatch.GRAVEYARD_ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY.trigger())) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        activatingPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} triggers from the graveyard on exhaust ability activation",
                        gameData.id, card.getName());
            }
        }
    }

    /**
     * Fires triggers for the creature tapped to saddle a Mount or crew a Vehicle during its
     * controller's main phase. The check runs once for each creature tapped to pay the cost.
     */
    public void checkSelfSaddlesOrCrewsDuringMainPhaseTriggers(GameData gameData, UUID activatingPlayerId,
                                                                Permanent tappedCreature,
                                                                UUID saddledOrCrewedPermanentId) {
        if (!activatingPlayerId.equals(gameData.activePlayerId)
                || (gameData.currentStep != TurnStep.PRECOMBAT_MAIN
                && gameData.currentStep != TurnStep.POSTCOMBAT_MAIN)) {
            return;
        }

        Permanent saddledOrCrewedPermanent = gameQueryService.findPermanentById(gameData, saddledOrCrewedPermanentId);
        if (saddledOrCrewedPermanent == null) return;
        if (!gameQueryService.hasEffectiveSubtype(gameData, saddledOrCrewedPermanent, CardSubtype.MOUNT)
                && !gameQueryService.hasEffectiveSubtype(gameData, saddledOrCrewedPermanent, CardSubtype.VEHICLE)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(activatingPlayerId);
        if (battlefield == null) return;
        Permanent watcher = battlefield.stream()
                .filter(permanent -> permanent.getId().equals(tappedCreature.getId()))
                .findFirst()
                .orElse(null);
        if (watcher == null || watcher.isLosesAllAbilitiesUntilEndOfTurn()) return;

        List<CardEffect> effects = new ArrayList<>(
                MiscWatch.ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE.effectsOf(watcher.getCard()));
        effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                gameData, watcher, MiscWatch.ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE.trigger()));
        if (effects.isEmpty()) return;

        int triggerCopies = 1 +
                gameQueryService.countAdditionalTriggeredAbilityTriggers(gameData, activatingPlayerId, watcher);
        for (CardEffect effect : effects) {
            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.getCard(),
                    activatingPlayerId,
                    watcher.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    watcher.getId());
            trigger.setNonTargeting(true);
            trigger.setSourcePermanentSnapshot(new Permanent(watcher));
            trigger.setTriggeringPermanentId(saddledOrCrewedPermanent.getId());
            gameData.pendingActivatedAbilityCostTriggers.add(trigger);
            for (int copy = 1; copy < triggerCopies; copy++) {
                gameData.pendingActivatedAbilityCostTriggers.add(new StackEntry(trigger));
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
            log.info("Game {} - {} triggers when its controller saddles or crews {}",
                    gameData.id, watcher.getCard().getName(), saddledOrCrewedPermanent.getCard().getName());
        }
    }

    private boolean checkDiscardEventTriggers(GameData gameData, UUID discardingPlayerId,
                                              int discardedCount, List<Card> discardedCards) {
        List<Permanent> ownBattlefield = gameData.playerBattlefields.get(discardingPlayerId);
        if (ownBattlefield == null) {
            return false;
        }

        boolean[] triggered = {false};
        var ctx = new TriggerContext.DiscardEvent(discardingPlayerId, discardedCount, discardedCards);
        for (Permanent perm : List.copyOf(ownBattlefield)) {
            if (dispatchWatch(gameData, perm, discardingPlayerId,
                    LibraryWatch.ON_CONTROLLER_DISCARD_EVENT, ctx)) {
                triggered[0] = true;
            }
        }
        return triggered[0];
    }

    /**
     * "Whenever you activate an eternalize or embalm ability" triggers (Vizier of the Anointed).
     * Fires once per activation on every permanent the activating player controls that has an
     * {@link SpellAbilityWatch#ON_CONTROLLER_ACTIVATES_ETERNALIZE_OR_EMBALM} effect. Called from the
     * graveyard-ability activation path only when the activated ability is an embalm/eternalize
     * ability, so no per-permanent condition is needed.
     */
    public void checkControllerActivatesEternalizeOrEmbalmTriggers(GameData gameData, UUID activatingPlayerId) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!ownerId.equals(activatingPlayerId)) return;
            for (CardEffect effect : SpellAbilityWatch.ON_CONTROLLER_ACTIVATES_ETERNALIZE_OR_EMBALM.effectsOf(perm.getCard())) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on eternalize/embalm activation",
                        gameData.id, perm.getCard().getName());
            }
        });
    }

    public record PreCostActivationTriggers(List<StackEntry> entries, List<CardEffect> effects) {}

    /**
     * Captures non-tap activation triggers before activation costs can remove the activated source
     * or orphan an Aura that watches it. The returned entries are added after the activated ability
     * is pushed onto the stack.
     */
    public PreCostActivationTriggers collectNonTapActivationTriggersBeforeCosts(
            GameData gameData, UUID activatingPlayerId, ActivatedAbility ability,
            Permanent activatedPermanent, UUID activatedPermanentControllerId) {
        List<StackEntry> entries = new ArrayList<>();
        List<CardEffect> effects = new ArrayList<>();
        gameData.forEachPermanent((ownerId, perm) -> {
            // Sacrificed for this activation's costs, so gone by the time the ability is activated.
            if (gameData.pendingActivationCostSacrificeIds.contains(perm.getId())) return;
            boolean watchesController = ownerId.equals(activatingPlayerId);
            SpellAbilityWatch watch = watchesController
                    ? SpellAbilityWatch.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY
                    : SpellAbilityWatch.ON_OPPONENT_ACTIVATES_NONMANA_ABILITY;
            for (CardEffect effect : watch.effectsOf(perm.getCard())) {
                if (!(effect instanceof TriggeringNonTapAbilityConditionalEffect)) continue;
                CardEffect resolved = effect.resolveForActivatedAbility(ability);
                if (resolved == null) continue;
                resolved = resolveTriggeringPermanentConditional(
                        gameData, perm, ownerId, activatedPermanent, resolved);
                if (resolved == null) continue;

                UUID targetId = activationTriggerTargetId(
                        resolved, null, activatingPlayerId, activatedPermanentControllerId);
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        targetId,
                        perm.getId());
                trigger.setNonTargeting(true);
                trigger.setTriggeringPermanentId(activatedPermanent.getId());
                trigger.setTriggeringPermanentControllerId(activatedPermanentControllerId);
                entries.add(trigger);
                effects.add(effect);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on non-tap ability activation ({})",
                        gameData.id, perm.getCard().getName(), activatedPermanent.getCard().getName());
            }
        });
        return new PreCostActivationTriggers(List.copyOf(entries), List.copyOf(effects));
    }

    /**
     * "Whenever an opponent activates an ability of {a permanent}, if it isn't a mana ability, ..."
     * triggers (e.g. Harsh Mentor). Fires on every permanent NOT controlled by the activating player
     * that has an {@link SpellAbilityWatch#ON_OPPONENT_ACTIVATES_NONMANA_ABILITY} effect, optionally filtered
     * (when wrapped in {@link TriggeringPermanentConditionalEffect}) by the permanent whose ability was
     * activated. Called only from the non-mana activation path, so mana abilities never trigger it (the
     * "if it isn't a mana ability" clause). The activating player is baked as the non-targeting
     * {@code targetId} so a player-directed effect (e.g. {@code DealDamageToPlayersEffect(2, TARGET_PLAYER)})
     * acts on "that player". The activated permanent's controller is also supplied separately so
     * effects referring to that controller still work when an activation cost removes the permanent.
     */
    public void checkOpponentActivatesNonManaAbilityTriggers(GameData gameData, UUID activatingPlayerId,
                                                              StackEntry abilityEntry, ActivatedAbility ability,
                                                              Permanent activatedPermanent,
                                                              UUID activatedPermanentControllerId,
                                                              List<CardEffect> preCollectedEffects) {
        if (abilityEntry == null) return;
        gameData.forEachPermanent((ownerId, perm) -> {
            if (ownerId.equals(activatingPlayerId)) return;
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) return;
            for (CardEffect effect : SpellAbilityWatch.ON_OPPONENT_ACTIVATES_NONMANA_ABILITY.effectsOf(perm.getCard())) {
                if (isPreCollectedActivationEffect(effect, preCollectedEffects)) continue;
                CardEffect resolved = effect.resolveForActivatedAbility(ability);
                if (resolved == null) continue;
                CardEffect authoredEffect = resolved;
                resolved = OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, perm, resolved);
                if (resolved == null) continue;
                resolved = resolveTriggeringPermanentConditional(
                        gameData, perm, ownerId, activatedPermanent, resolved);
                if (resolved == null) continue;
                UUID targetId = activationTriggerTargetId(
                        resolved, abilityEntry, activatingPlayerId, activatedPermanentControllerId);
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        targetId,
                        perm.getId());
                // "That player" is the opponent who activated the ability — set by the event, not chosen.
                trigger.setNonTargeting(true);
                if (authoredEffect instanceof OncePerTurnTriggerEffect once && once.markOnAcceptance()) {
                    trigger.setMarkSourceOncePerTurnOnAcceptance(true);
                }
                gameData.enqueueTrigger(trigger);
                OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, authoredEffect);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on opponent non-mana ability activation ({})",
                        gameData.id, perm.getCard().getName(), abilityEntry.getCard().getName());
            }
        });
    }

    /**
     * Unwraps a {@link TriggeringPermanentConditionalEffect} against the permanent whose event fired.
     * Returns the wrapped effect when the predicate matches (evaluated from {@code watcher}'s point of
     * view), the effect unchanged when it isn't a conditional, or {@code null} to signal the trigger
     * should be skipped for this watcher. Shared by the ability-activation trigger collectors.
     */
    private CardEffect resolveTriggeringPermanentConditional(GameData gameData, Permanent watcher, UUID watcherOwnerId,
                                                             Permanent triggeringPermanent, CardEffect effect) {
        if (!(effect instanceof TriggeringPermanentConditionalEffect conditional)) {
            return effect;
        }
        if (triggeringPermanent == null) return null;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(watcher.getOriginalCard().getId())
                .withSourceControllerId(watcherOwnerId)
                .withSourcePermanentId(watcher.getId())
                .withSourcePermanentSnapshot(watcher);
        if (!predicateEvaluationService.matchesPermanentPredicate(triggeringPermanent, conditional.predicate(), filterContext)) {
            return null;
        }
        return conditional.wrapped();
    }

    /**
     * "Whenever you activate an ability, if it isn't a mana ability, you may pay {N} to copy it"
     * triggers (Rings of Brighthearth). Called after the non-mana ability has been put on the stack
     * so it can be snapshotted. Fires on every permanent the activating player controls that has an
     * {@link SpellAbilityWatch#ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY} effect — except for
     * {@code equippedCreatureOnly} triggers (Illusionist's Bracers), which instead fire on any
     * permanent attached to the ability's source permanent, and copy the ability for free.
     *
     * @param abilityEntry the activated ability's stack entry (already on the stack)
     * @param ability      the activated ability that was activated (retained for retargeting the copy)
     */
    public void checkControllerActivatesNonManaAbilityTriggers(GameData gameData, UUID activatingPlayerId,
                                                               StackEntry abilityEntry, ActivatedAbility ability,
                                                               Permanent activatedPermanent,
                                                               UUID activatedPermanentControllerId,
                                                               List<CardEffect> preCollectedEffects) {
        if (abilityEntry == null) return;

        final int activationLifePaid = gameData.activationLifePaid;
        gameData.trackingActivationLifePaid = false;
        gameData.activationLifePaid = 0;

        Integer pendingLoyaltyCopies = gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.get(activatingPlayerId);
        if (pendingLoyaltyCopies != null && pendingLoyaltyCopies > 0 && ability.getLoyaltyCost() != null) {
            StackEntry snapshot = new StackEntry(abilityEntry);
            List<GameData.PendingCopySource> sources =
                    gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn.getOrDefault(activatingPlayerId, List.of());
            for (int i = 0; i < pendingLoyaltyCopies; i++) {
                GameData.PendingCopySource source = i < sources.size() ? sources.get(i) : null;
                Card copySourceCard = source != null ? source.card() : abilityEntry.getCard();
                UUID copySourcePermanentId = source != null ? source.sourcePermanentId() : null;
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        copySourceCard,
                        activatingPlayerId,
                        "Copy " + abilityEntry.getCard().getName() + "'s loyalty ability",
                        List.of(new CopyControllerActivatedAbilityEffect(snapshot, ability, activatingPlayerId)),
                        0,
                        copySourcePermanentId
                ));
            }
            gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.remove(activatingPlayerId);
            gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn.remove(activatingPlayerId);
            if (gameData.pendingNextSpellOrLoyaltyCopyLinkedThisTurn.remove(activatingPlayerId)) {
                gameData.pendingNextInstantSorceryCopyThisTurnCount.remove(activatingPlayerId);
                gameData.pendingNextInstantSorceryCopyThisTurnSingleTriggerCount.remove(activatingPlayerId);
            }
        }

        Integer pendingExhaustCopies = gameData.pendingNextExhaustAbilityCopyThisTurnCount.get(activatingPlayerId);
        if (pendingExhaustCopies != null && pendingExhaustCopies > 0 && ability.isExhaustAbility()) {
            StackEntry snapshot = new StackEntry(abilityEntry);
            List<GameData.PendingCopySource> sources = gameData.pendingNextExhaustAbilityCopySourcesThisTurn
                    .getOrDefault(activatingPlayerId, List.of());
            for (int i = 0; i < pendingExhaustCopies; i++) {
                GameData.PendingCopySource source = i < sources.size() ? sources.get(i) : null;
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source != null ? source.card() : abilityEntry.getCard(),
                        activatingPlayerId,
                        "Copy " + abilityEntry.getCard().getName() + "'s exhaust ability",
                        new ArrayList<>(List.of(
                                new CopyControllerActivatedAbilityEffect(snapshot, ability, activatingPlayerId))),
                        0,
                        source != null ? source.sourcePermanentId() : null
                ));
            }
            gameData.pendingNextExhaustAbilityCopyThisTurnCount.remove(activatingPlayerId);
            gameData.pendingNextExhaustAbilityCopySourcesThisTurn.remove(activatingPlayerId);
            gameLogService.append(gameData, GameLog.cardThen(abilityEntry.getCard(), "'s exhaust ability is copied."));
            log.info("Game {} - {} delayed exhaust-ability copy trigger(s) queued for {}",
                    gameData.id, pendingExhaustCopies, abilityEntry.getCard().getName());
        }

        for (GameData.ChosenSubtypeActivationCopy delayedCopy : gameData.chosenSubtypeActivationCopiesThisTurn) {
            if (!delayedCopy.controllerId().equals(activatingPlayerId) || activatedPermanent == null
                    || !predicateEvaluationService.matchesPermanentPredicate(activatedPermanent,
                    new PermanentAllOfPredicate(List.of(
                            new PermanentIsPlaneswalkerPredicate(),
                            new PermanentHasSubtypePredicate(delayedCopy.subtype()))),
                    FilterContext.of(gameData).withSourceControllerId(activatingPlayerId))) {
                continue;
            }
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayedCopy.sourceCard(),
                    activatingPlayerId,
                    delayedCopy.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(new CopyControllerActivatedAbilityEffect(
                            new StackEntry(abilityEntry), ability, activatingPlayerId))),
                    null,
                    delayedCopy.sourcePermanentId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(delayedCopy.sourceCard()));
            log.info("Game {} - {} delayed trigger copies {}'s ability", gameData.id,
                    delayedCopy.sourceCard().getName(), abilityEntry.getCard().getName());
        }

        gameData.forEachPermanent((ownerId, perm) -> {
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) return;
            for (CardEffect effect : SpellAbilityWatch.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY.effectsOf(perm.getCard())) {
                if (isPreCollectedActivationEffect(effect, preCollectedEffects)) continue;
                if (!(effect instanceof CopyControllerActivatedAbilityTriggerEffect trigger)) {
                    if (!ownerId.equals(activatingPlayerId)) continue;
                    CardEffect resolved = effect.resolveForActivatedAbility(ability);
                    if (resolved == null) continue;
                    resolved = resolveTriggeringPermanentConditional(
                            gameData, perm, ownerId, activatedPermanent, resolved);
                    if (resolved == null) continue;
                    if (resolved.targetSpec().declares(TargetPredicates.anyTarget())) {
                        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                                perm.getCard(), ownerId, new ArrayList<>(List.of(resolved)), false, null, 0,
                                perm.getId()));
                        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                        log.info("Game {} - {} queues an any-target non-mana ability trigger ({})",
                                gameData.id, perm.getCard().getName(), abilityEntry.getCard().getName());
                    } else {
                        UUID targetId = activationTriggerTargetId(
                                resolved, abilityEntry, null, activatedPermanentControllerId);
                        StackEntry activationTrigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                ownerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(resolved)),
                                targetId,
                                perm.getId()
                        );
                        if (targetId != null) {
                            activationTrigger.setNonTargeting(true);
                        }
                        gameData.enqueueTrigger(activationTrigger);
                        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                        log.info("Game {} - {} triggers on non-mana ability activation ({})",
                                gameData.id, perm.getCard().getName(), abilityEntry.getCard().getName());
                    }
                    continue;
                }
                if (trigger.equippedCreatureOnly()) {
                    // "an ability of equipped creature" — the ability's source must be what this
                    // permanent is attached to, whoever activated it.
                    if (perm.getAttachedTo() == null
                            || !perm.getAttachedTo().equals(abilityEntry.getSourcePermanentId())) {
                        continue;
                    }
                } else if (!ownerId.equals(activatingPlayerId)) {
                    continue;
                }
                if (trigger.sourceFilter() != null && !targetLegalityService.matchesStackEntryPredicate(
                        gameData, abilityEntry, trigger.sourceFilter(), ownerId, perm)) {
                    continue;
                }
                if (trigger.sourcePermanentFilter() != null
                        && (activatedPermanent == null || !predicateEvaluationService.matchesPermanentPredicate(
                        activatedPermanent,
                        trigger.sourcePermanentFilter(),
                        FilterContext.of(gameData)
                                .withSourceControllerId(ownerId)
                                .withSourcePermanentSnapshot(perm)))) {
                    continue;
                }
                if (trigger.targetPredicate() != null && !targetLegalityService.matchesStackEntryPredicate(
                        gameData, abilityEntry, trigger.targetPredicate(), ownerId)) {
                    continue;
                }
                if (trigger.requiresXInActivationCost()
                        && (ability.getManaCost() == null || !new ManaCost(ability.getManaCost()).hasX())) {
                    continue;
                }
                if (trigger.loyaltyAbilityOnly() && ability.getLoyaltyCost() == null) {
                    continue;
                }
                if (trigger.exhaustAbilityOnly() && !ability.isExhaustAbility()) {
                    continue;
                }
                if (trigger.requiresSacrificedPermanent()
                        && abilityEntry.getSacrificedCardIds().isEmpty()
                        && ability.getEffects().stream()
                        .filter(CostEffect.class::isInstance)
                        .map(CostEffect.class::cast)
                        .noneMatch(CostEffect::sacrificesSourcePermanent)) {
                    continue;
                }

                int lifePaid = trigger.lifePaymentOnly() ? activationLifePaid : 0;
                if (trigger.lifePaymentOnly() && lifePaid <= 0) {
                    continue;
                }

                StackEntry snapshot = new StackEntry(abilityEntry);
                // CR 707.10 — the copy is controlled by the controller of the effect that created it.
                UUID copyControllerId = trigger.equippedCreatureOnly() ? ownerId : activatingPlayerId;
                CardEffect copyEffect = new CopyControllerActivatedAbilityEffect(
                        snapshot, ability, copyControllerId);
                if (trigger.lifePaymentOnly()) {
                    copyEffect = new MayPayLifeEqualToAmountEffect(
                            new EventValue(), copyEffect,
                            "Pay that much life to copy " + abilityEntry.getCard().getName() + "'s ability?");
                } else if (trigger.manaCost() != null) {
                    copyEffect = new MayPayManaEffect(
                            trigger.manaCost(),
                            copyEffect,
                            "Pay " + trigger.manaCost() + " to copy " + abilityEntry.getCard().getName() + "'s ability?");
                } else if (trigger.optionalCopy()) {
                    copyEffect = new MayEffect(
                            copyEffect,
                            "Copy " + abilityEntry.getCard().getName() + "'s ability?");
                }

                StackEntry copyTrigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(copyEffect)),
                        null,
                        perm.getId()
                );
                if (trigger.lifePaymentOnly()) {
                    copyTrigger.setEventValue(lifePaid);
                }
                gameData.enqueueTrigger(copyTrigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on non-mana ability activation ({})",
                        gameData.id, perm.getCard().getName(), abilityEntry.getCard().getName());
            }
        });
    }

    /**
     * "Whenever an opponent activates an ability of {a permanent}" triggers that include mana
     * abilities. The activating player is the opponent for every watcher that is not on that
     * player's battlefield.
     */
    public void checkOpponentActivatesAbilityTriggers(GameData gameData, UUID activatingPlayerId,
                                                      Permanent activatedPermanent, ActivatedAbility ability,
                                                      UUID activatedPermanentControllerId,
                                                      List<CardEffect> preCollectedEffects) {
        gameData.forEachPermanent((ownerId, perm) -> {
            if (ownerId.equals(activatingPlayerId)) return;
            if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) return;
            for (CardEffect effect : SpellAbilityWatch.ON_OPPONENT_ACTIVATES_ABILITY.effectsOf(perm.getCard())) {
                if (isPreCollectedActivationEffect(effect, preCollectedEffects)) continue;
                CardEffect resolved = effect.resolveForActivatedAbility(ability);
                if (resolved == null) continue;
                resolved = resolveTreasureManaActivationConditional(gameData, activatedPermanent, resolved);
                if (resolved == null) continue;
                resolved = resolveTriggeringPermanentConditional(gameData, perm, ownerId, activatedPermanent, resolved);
                if (resolved == null) continue;
                if (resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        && !resolved.targetSpec().declares(TargetPredicates.anyTarget())) {
                    gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved)),
                            "ability activation", perm.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} queues a permanent-target opponent ability-activation trigger ({})",
                            gameData.id, perm.getCard().getName(), activatedPermanent.getCard().getName());
                    continue;
                }
                if (resolved.targetSpec().declares(TargetPredicates.anyTarget())) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            perm.getCard(), ownerId, new ArrayList<>(List.of(resolved)), false, null, 0, perm.getId()));
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    continue;
                }
                UUID targetId = activationTriggerTargetId(
                        resolved, null, activatingPlayerId, activatedPermanentControllerId);
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        targetId,
                        perm.getId());
                if (targetId != null) {
                    trigger.setNonTargeting(true);
                }
                trigger.setTriggeringPermanentId(activatedPermanent.getId());
                trigger.setTriggeringPermanentControllerId(activatedPermanentControllerId);
                gameData.enqueueTrigger(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} triggers on opponent ability activation ({})",
                        gameData.id, perm.getCard().getName(), activatedPermanent.getCard().getName());
            }
        });
    }

    private UUID activationTriggerTargetId(CardEffect effect, StackEntry abilityEntry,
                                           UUID defaultTargetId, UUID activatedPermanentControllerId) {
        if (EffectResolution.targetsSpellOnStack(effect)) {
            return abilityEntry == null ? null : abilityEntry.getTargetableId();
        }
        if (effect instanceof DealDamageToPlayersEffect damage
                && damage.recipient() == DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER) {
            return activatedPermanentControllerId;
        }
        return defaultTargetId;
    }

    /** Resolves Vazi-style intervening-if conditions before an ability-activation trigger is queued. */
    private CardEffect resolveTreasureManaActivationConditional(GameData gameData,
                                                                Permanent activatedPermanent,
                                                                CardEffect effect) {
        if (!(effect instanceof ConditionalEffect conditional)
                || !conditional.interveningIf()
                || !(conditional.condition() instanceof TreasureManaSpentToActivate)) {
            return effect;
        }
        return gameData.abilityActivationUsedTreasureMana
                .getOrDefault(activatedPermanent.getCard().getId(), false)
                ? conditional.wrapped() : null;
    }

    private boolean isPreCollectedActivationEffect(CardEffect effect, List<CardEffect> preCollectedEffects) {
        return preCollectedEffects != null
                && preCollectedEffects.stream().anyMatch(preCollected -> preCollected == effect);
    }

    // ── Life-loss triggers ─────────────────────────────────────────────

    public void checkLifeLossTriggers(GameData gameData, UUID losingPlayerId, int lifeLostAmount) {
        if (lifeLostAmount <= 0) return;

        // Accumulate life lost this turn (damage funnels through here too — "damage causes loss of
        // life"). Read by Wound Reflection at end of turn.
        gameData.lifeLostThisTurn.merge(losingPlayerId, lifeLostAmount, Integer::sum);
        increaseActivePlayerSpeedIfEligible(gameData, losingPlayerId);

        boolean[] anyTriggered = {false};
        var ctx = new TriggerContext.LifeLoss(losingPlayerId, lifeLostAmount);

        // Snapshot: handlers may modify the battlefield (e.g. Mindcrank mills → Undead Alchemist creates tokens)
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(losingPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                for (CardEffect effect : LifeWatch.ON_OPPONENT_LOSES_LIFE.effectsOf(perm.getCard())) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    if (dispatch(match, LifeWatch.ON_OPPONENT_LOSES_LIFE, effect, ctx)) {
                        anyTriggered[0] = true;
                    }
                }
            }
        });

        // Controller-loses-life triggers (e.g. Lich's Mastery)
        // Snapshot: handlers may modify the battlefield (exile permanents)
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (!playerId.equals(losingPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                if (gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                for (CardEffect effect : LifeWatch.ON_CONTROLLER_LOSES_LIFE.effectsOf(perm.getCard())) {
                    CardEffect toDispatch = effect;
                    if (effect instanceof OncePerTurnTriggerEffect once) {
                        if (once.firstLifeLoss()) {
                            if (gameData.lifeLostThisTurn.getOrDefault(playerId, 0) != lifeLostAmount) {
                                continue;
                            }
                        } else if (gameData.oncePerTurnTriggersFiredThisTurn.contains(perm.getId())) {
                            continue;
                        }
                        toDispatch = once.wrapped();
                    }
                    toDispatch = unwrapLifeChangeConditional(gameData, perm, playerId, toDispatch);
                    if (toDispatch == null) {
                        continue;
                    }
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    if (registry.dispatch(match, LifeWatch.ON_CONTROLLER_LOSES_LIFE, toDispatch, ctx)) {
                        anyTriggered[0] = true;
                        if (effect instanceof OncePerTurnTriggerEffect once && !once.firstLifeLoss()) {
                            gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                        }
                    }
                }
            }
        });

        if (anyTriggered[0]) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }

    // ── Life-gain triggers ──────────────────────────────────────────────

    /** Fires triggers for life actually paid by a player, distinct from ordinary life loss. */
    public void checkLifePaymentTriggers(GameData gameData, UUID payingPlayerId, int lifePaidAmount) {
        if (lifePaidAmount <= 0) return;
        if (gameData.trackingActivationLifePaid) {
            gameData.activationLifePaid += lifePaidAmount;
        }

        var ctx = new TriggerContext.LifePayment(payingPlayerId, lifePaidAmount);
        List<Permanent> battlefield = gameData.playerBattlefields.get(payingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            List<CardEffect> effects = new ArrayList<>(
                    LifeWatch.ON_CONTROLLER_PAYS_LIFE.effectsOf(perm.getCard()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, perm, LifeWatch.ON_CONTROLLER_PAYS_LIFE.trigger()));
            for (CardEffect effect : effects) {
                CardEffect toDispatch = effect;
                if (effect instanceof OncePerTurnTriggerEffect once) {
                    if (gameData.oncePerTurnTriggersFiredThisTurn.contains(perm.getId())) {
                        continue;
                    }
                    toDispatch = once.wrapped();
                }
                var match = new TriggerMatchContext(gameData, perm, payingPlayerId, effect);
                if (dispatch(match, LifeWatch.ON_CONTROLLER_PAYS_LIFE, toDispatch, ctx)
                        && effect instanceof OncePerTurnTriggerEffect) {
                    gameData.oncePerTurnTriggersFiredThisTurn.add(perm.getId());
                }
            }
        }
    }

    /** Fires triggers when an opponent pays mana imposed by a spell or permanent controlled by a player. */
    public void checkTaxPaymentTriggers(GameData gameData, UUID payingPlayerId,
                                        UUID taxingPlayerId, int manaPaid) {
        if (payingPlayerId == null || taxingPlayerId == null
                || payingPlayerId.equals(taxingPlayerId) || manaPaid <= 0) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(taxingPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.TaxPayment(payingPlayerId, taxingPlayerId, manaPaid);
        for (Permanent perm : List.copyOf(battlefield)) {
            List<CardEffect> effects = new ArrayList<>(
                    ActionWatch.ON_OPPONENT_PAYS_TAX.effectsOf(perm.getCard()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, perm, ActionWatch.ON_OPPONENT_PAYS_TAX.trigger()));
            for (CardEffect effect : effects) {
                CardEffect toDispatch = unwrapOncePerTurnTrigger(gameData, perm, effect);
                if (toDispatch == null) continue;
                var match = new TriggerMatchContext(gameData, perm, taxingPlayerId, effect);
                dispatch(match, ActionWatch.ON_OPPONENT_PAYS_TAX, toDispatch, ctx);
            }
        }
    }

    private void increaseActivePlayerSpeedIfEligible(GameData gameData, UUID losingPlayerId) {
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null || activePlayerId.equals(losingPlayerId)) {
            return;
        }
        int speed = gameData.playerSpeeds.getOrDefault(activePlayerId, 0);
        if (speed < 1 || speed >= 4 || !gameData.playersWhoseSpeedIncreasedThisTurn.add(activePlayerId)) {
            return;
        }
        Card source = new Card();
        source.setName("Speed");
        source.setOwnerId(activePlayerId);
        source.freeze();
        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source, activePlayerId,
                "Increase speed", List.of(new com.github.laxika.magicalvibes.model.effect.IncreaseControllerSpeedEffect()),
                (UUID) null, (UUID) null);
        trigger.setNonTargeting(true);
        gameData.enqueueTrigger(trigger);
    }

    public void checkLifeGainTriggers(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount) {
        checkLifeGainTriggers(gameData, gainingPlayerId, lifeGainedAmount, null, null);
    }

    public void checkLifeGainTriggers(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount,
            Card sourceCard, StackEntryType sourceEntryType) {
        if (lifeGainedAmount <= 0) return;

        var ctx = new TriggerContext.LifeGain(gainingPlayerId, lifeGainedAmount, sourceCard, sourceEntryType);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (!playerId.equals(gainingPlayerId)) return;

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = new ArrayList<>();
                if (!gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
                    effects.addAll(LifeWatch.ON_CONTROLLER_GAINS_LIFE.effectsOf(perm.getCard()));
                }
                effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                        gameData, perm, LifeWatch.ON_CONTROLLER_GAINS_LIFE.trigger()));
                for (CardEffect effect : effects) {
                    CardEffect toDispatch = effect;
                    if (effect instanceof OncePerTurnTriggerEffect once) {
                        if (once.firstLifeGain()
                                && gameData.getLifeGainedThisTurn(gainingPlayerId) != lifeGainedAmount) {
                            continue;
                        }
                        if (!once.markOnAcceptance()
                                && OncePerTurnTriggerSupport.isFired(gameData, perm, once)) {
                            continue;
                        }
                        toDispatch = once.wrapped();
                    }
                    toDispatch = unwrapLifeChangeConditional(gameData, perm, playerId, toDispatch);
                    if (toDispatch == null) {
                        continue;
                    }
                    boolean markOnAcceptance = effect instanceof OncePerTurnTriggerEffect once
                            && once.markOnAcceptance();
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect,
                            perm.getCard(), markOnAcceptance);
                    if (dispatch(match, LifeWatch.ON_CONTROLLER_GAINS_LIFE, toDispatch, ctx)) {
                        OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, effect);
                    }
                }
            }
        });

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(gainingPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                dispatchWatch(gameData, perm, playerId, LifeWatch.ON_OPPONENT_GAINS_LIFE, ctx);
            }
        });

        collectGraveyardControllerLifeGainTriggers(gameData, gainingPlayerId, lifeGainedAmount);
        collectGraveyardOpponentLifeGainTriggers(gameData, gainingPlayerId);
        collectLifeGainOpponentLifeLossTriggers(gameData, gainingPlayerId, lifeGainedAmount);
        collectTemporaryGlobalTriggers(gameData, LifeWatch.ON_ANY_PLAYER_GAINS_LIFE, gainingPlayerId, lifeGainedAmount);
        collectEmblemLifeGainTriggers(gameData, gainingPlayerId, lifeGainedAmount);
    }

    /**
     * Fires emblem triggers whose text starts "Whenever you gain life". Emblems live outside the
     * battlefield, so the permanent sweep in {@link #checkLifeGainTriggers} cannot see them.
     */
    private void collectEmblemLifeGainTriggers(GameData gameData, UUID gainingPlayerId,
                                                int lifeGainedAmount) {
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(gainingPlayerId)) continue;

            for (CardEffect effect : emblem.staticEffects()) {
                if (!(effect instanceof EmblemLifeGainTriggerEffect trigger)) continue;

                Card source = emblem.sourceCard();
                String description = (source != null ? source.getName() : "Emblem") + "'s emblem";
                UUID opponentId = gameQueryService.getOpponentId(gameData, gainingPlayerId);
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source,
                        gainingPlayerId,
                        description,
                        new ArrayList<>(trigger.effects()),
                        opponentId,
                        (UUID) null
                );
                entry.setEventValue(lifeGainedAmount);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.text(
                        description + " triggers — target opponent loses that much life."));
                log.info("Game {} - {} triggers on life gain ({} life)",
                        gameData.id, description, lifeGainedAmount);
            }
        }
    }

    /** Fires triggers for a player's positive energy-counter change. */
    public void checkEnergyGainTriggers(GameData gameData, UUID gainingPlayerId, int energyGainedAmount) {
        if (energyGainedAmount <= 0) return;

        var ctx = new TriggerContext.EnergyGain(gainingPlayerId, energyGainedAmount);
        List<Permanent> battlefield = gameData.playerBattlefields.get(gainingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchWatch(gameData, perm, gainingPlayerId, CounterWatch.ON_CONTROLLER_GETS_ENERGY, ctx);
        }
    }

    /** Fires controller-scoped triggers once for each proliferate event. */
    public void checkProliferateTriggers(GameData gameData, UUID proliferatingPlayerId,
                                         int proliferateCount) {
        if (proliferateCount <= 0) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(proliferatingPlayerId);
        if (battlefield != null) {
            TriggerContext.Proliferate ctx = new TriggerContext.Proliferate(proliferatingPlayerId);
            for (Permanent perm : List.copyOf(battlefield)) {
                for (int i = 0; i < proliferateCount; i++) {
                    dispatchWatch(gameData, perm, proliferatingPlayerId,
                            CounterWatch.ON_CONTROLLER_PROLIFERATES, ctx);
                }
            }
        }

        List<Card> graveyard = gameData.playerGraveyards.get(proliferatingPlayerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            List<CardEffect> effects = gameQueryService.getEffectiveGraveyardTriggerEffects(
                    gameData, card, CounterWatch.GRAVEYARD_ON_CONTROLLER_PROLIFERATES.trigger());
            if (effects == null || effects.isEmpty()) continue;

            for (int i = 0; i < proliferateCount; i++) {
                for (CardEffect effect : effects) {
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            proliferatingPlayerId,
                            card.getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    );
                    entry.setTriggeringCardId(card.getId());
                    entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                    gameData.enqueueTrigger(entry);
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    log.info("Game {} - {} graveyard trigger queued on proliferate",
                            gameData.id, card.getName());
                }
            }
        }
    }

    private void collectGraveyardOpponentLifeGainTriggers(GameData gameData, UUID gainingPlayerId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(gainingPlayerId)) continue;

            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;

            for (Card card : new ArrayList<>(graveyard)) {
                for (CardEffect effect : gameQueryService.getEffectiveGraveyardTriggerEffects(
                        gameData, card, LifeWatch.GRAVEYARD_ON_OPPONENT_GAINS_LIFE.trigger())) {
                    gameData.enqueueTrigger(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            playerId,
                            card.getName() + "'s ability",
                            new ArrayList<>(List.of(effect))
                    ));
                    gameLogService.append(gameData, GameLog.abilityTriggers(card));
                    log.info("Game {} - {} triggers from the graveyard when an opponent gains life",
                            gameData.id, card.getName());
                }
            }
        }
    }

    /**
     * Fires the turn-scoped "whenever you gain life this turn, each opponent loses that much life"
     * delayed triggers (Vizkopa Guildmage). One trigger per watcher whose controller is the player
     * who gained life; the amount rides on the entry's event value.
     */
    private void collectLifeGainOpponentLifeLossTriggers(GameData gameData, UUID gainingPlayerId,
                                                        int lifeGainedAmount) {
        for (LifeGainOpponentLifeLossWatcher watcher : List.copyOf(gameData.lifeGainOpponentLifeLossWatchers)) {
            if (!watcher.controllerId().equals(gainingPlayerId)) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new LoseLifeEffect(new EventValue(), LoseLifeRecipient.EACH_OPPONENT))),
                    (UUID) null,
                    (UUID) null);
            entry.setEventValue(lifeGainedAmount);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers (controller gained {} life), each opponent loses that much life",
                    gameData.id, watcher.sourceCard().getName(), lifeGainedAmount);
        }
    }

    // ── Creature-card-milled triggers ─────────────────────────────────

    // ── Noncombat-damage-to-opponent triggers ──────────────────────────

    public void checkNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId) {
        checkNoncombatDamageToOpponentTriggers(gameData, damagedPlayerId, null, 0);
    }

    public void checkNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int damageAmount) {
        var ctx = new TriggerContext.NoncombatDamageToOpponent(
                damagedPlayerId, sourceControllerId, damageAmount);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(damagedPlayerId)) return;

            for (Permanent perm : battlefield) {
                for (CardEffect effect : DamageWatch.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE.effectsOf(perm.getCard())) {
                    var match = new TriggerMatchContext(gameData, perm, playerId, effect);
                    dispatch(match, DamageWatch.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE, effect, ctx);
                }
            }
        });

        if (sourceControllerId == null || sourceControllerId.equals(damagedPlayerId)) return;

        List<Card> graveyard = gameData.playerGraveyards.get(sourceControllerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            List<CardEffect> effects = gameQueryService.getEffectiveGraveyardTriggerEffects(
                    gameData, card, DamageWatch.GRAVEYARD_ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT.trigger());
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        sourceControllerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
            }
        }
    }

    /**
     * Chandra's Phoenix: "Whenever an opponent is dealt damage by a red instant or sorcery spell you
     * control or by a red planeswalker you control, return this card from your graveyard to your hand."
     *
     * <p>{@code entry} is the stack entry that dealt the damage. It qualifies when it is a red instant
     * or sorcery spell, or an ability whose source card is a red planeswalker. The graveyard scanned is
     * the entry controller's ("you control"), and the damaged player must be one of their opponents.
     */
    public void checkRedSpellOrPlaneswalkerDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
                                                                    StackEntry entry) {
        if (entry == null) return;

        UUID controllerId = entry.getControllerId();
        if (controllerId == null || controllerId.equals(damagedPlayerId)) return;

        Card sourceCard = entry.getEffectiveDamageSourceCard();
        if (sourceCard == null) return;
        // An ability's source permanent is judged by its current characteristics (a planeswalker
        // made blue by Prismatic Lace isn't red), falling back to last-known information.
        Permanent sourcePermanent = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourcePermanent == null) {
            sourcePermanent = entry.getSourcePermanentSnapshot();
        }
        boolean red = sourcePermanent != null
                ? gameQueryService.getEffectiveColors(gameData, sourcePermanent).contains(CardColor.RED)
                : sourceCard.getColors().contains(CardColor.RED);
        if (!red) return;

        boolean redSpell = entry.getEntryType() == StackEntryType.INSTANT_SPELL
                || entry.getEntryType() == StackEntryType.SORCERY_SPELL;
        boolean redPlaneswalker = sourcePermanent != null
                ? gameQueryService.isPlaneswalker(gameData, sourcePermanent)
                : sourceCard.hasType(CardType.PLANESWALKER);
        if (!redSpell && !redPlaneswalker) return;

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            List<CardEffect> effects =
                    gameQueryService.getEffectiveGraveyardTriggerEffects(
                            gameData, card, DamageWatch.GRAVEYARD_ON_OPPONENT_DAMAGED_BY_RED_SPELL_OR_PLANESWALKER.trigger());
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
            }
        }
    }

    private void collectAllyCreatureBecomesTargetOfBackupAbilityTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)
                || triggeringEntry.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                || triggeringEntry.getCard() == null
                || !triggeringEntry.getCard().getKeywords().contains(Keyword.BACKUP)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            if (source.isBackupAbilityCopyUsedThisTurn()) continue;

            List<CardEffect> effects = new ArrayList<>(TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_BACKUP_ABILITY.effectsOf(source.getCard()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source, TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_BACKUP_ABILITY.trigger()));
            if (effects.isEmpty()) continue;

            source.setBackupAbilityCopyUsedThisTurn(true);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    creatureControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(effects),
                    triggeringEntry.getTargetableId(),
                    source.getId()
            );
            entry.setNonTargeting(true);
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    "'s triggered ability triggers and copies the backup ability."));
            log.info("Game {} - {} backup-ability copy trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    /**
     * Bloodfeather Phoenix: "Whenever an instant or sorcery spell you control deals damage to an
     * opponent or battle". The caller has already batched the spell's damage event and supplies
     * whether that event hit a qualifying recipient.
     */
    public void checkInstantOrSorceryDamageToOpponentOrBattleTriggers(GameData gameData,
                                                                       Card sourceCard,
                                                                       UUID sourceControllerId,
                                                                       boolean damagedOpponentOrBattle) {
        if (!damagedOpponentOrBattle || sourceCard == null || sourceControllerId == null
                || (!sourceCard.hasType(CardType.INSTANT) && !sourceCard.hasType(CardType.SORCERY))) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(sourceControllerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            List<CardEffect> effects = gameQueryService.getEffectiveGraveyardTriggerEffects(
                    gameData, card, DamageWatch.GRAVEYARD_ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT_OR_BATTLE.trigger());
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        sourceControllerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                ));
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
            }
        }
    }

    // ── Queue-processing delegates ─────────────────────────────────────

    public void processNextDeathTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDeathTriggerTarget(gameData);
    }

    public boolean beginDeathGraveyardTargetAfterPermanent(GameData gameData,
            PermanentChoiceContext.DeathTriggerTarget pending, UUID permanentTargetId) {
        return triggeredAbilityQueueService.beginDeathGraveyardTargetAfterPermanent(
                gameData, pending, permanentTargetId);
    }

    public void processNextAttackTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextAttackTriggerTarget(gameData);
    }

    public TargetFilter targetFilterForTriggeredEffects(Card sourceCard, List<CardEffect> effects) {
        return triggeredAbilityQueueService.targetFilterForTriggeredEffects(sourceCard, effects);
    }

    public void processNextAttackCounterMoveFirstTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextAttackCounterMoveFirstTarget(gameData);
    }

    public List<UUID> targetableCreaturesControlledBy(GameData gameData, UUID playerId,
                                                      Card sourceCard, UUID choosingPlayerId) {
        return triggeredAbilityQueueService.targetableCreaturesControlledBy(gameData, playerId, sourceCard, choosingPlayerId);
    }

    public void processNextEntersTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEntersTriggerTarget(gameData);
    }

    public void processNextSelfTriggeredAbilityTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSelfTriggeredAbilityTarget(gameData);
    }

    public void processNextTriggeredModalTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextTriggeredModalTrigger(gameData);
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, ChooseOneEffect.ChooseOneOption chosen) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen);
    }

    public void processNextDiscardSelfTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextDiscardSelfTrigger(gameData);
    }

    public void processNextDiscardControllerTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDiscardControllerTriggerTarget(gameData);
    }

    public void processNextSpellTargetTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextSpellTargetTrigger(gameData);
    }

    public boolean beginTriggerOrder(GameData gameData) {
        return triggeredAbilityQueueService.beginTriggerOrder(gameData);
    }

    public void processNextETBSpellTargetTrigger(GameData gameData) {
        etbTokenTargetService.processNextETBSpellTargetTrigger(gameData);
    }

    public void processNextSpellGraveyardTargetTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextSpellGraveyardTargetTrigger(gameData);
    }

    public void processNextEmblemTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEmblemTriggerTarget(gameData);
    }

    public void processNextLifeGainTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextLifeGainTriggerTarget(gameData);
    }

    public void processNextDrawTriggerTarget(GameData gameData) {
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DrawTriggerAnyTarget.class)) {
            triggeredAbilityQueueService.processNextDrawTriggerTarget(gameData);
        } else if (gameData.hasPendingInteraction(PermanentChoiceContext.DrawTriggerPermanentTarget.class)) {
            triggeredAbilityQueueService.processNextDrawTriggerPermanentTarget(gameData);
        }
    }

    private void collectGraveyardControllerLifeGainTriggers(GameData gameData, UUID gainingPlayerId, int lifeGainedAmount) {
        List<Card> graveyard = gameData.playerGraveyards.get(gainingPlayerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            for (CardEffect effect : gameQueryService.getEffectiveGraveyardTriggerEffects(
                    gameData, card, LifeWatch.GRAVEYARD_ON_CONTROLLER_GAINS_LIFE.trigger())) {
                CardEffect resolved = effect;
                boolean oncePerTurn = effect instanceof OncePerTurnTriggerEffect;
                if (oncePerTurn) {
                    if (((OncePerTurnTriggerEffect) effect).firstLifeGain()
                            && gameData.getLifeGainedThisTurn(gainingPlayerId) != lifeGainedAmount) {
                        continue;
                    }
                    if (gameData.oncePerTurnTriggersFiredThisTurn.contains(card.getId())) {
                        continue;
                    }
                    resolved = ((OncePerTurnTriggerEffect) effect).wrapped();
                }

                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        gainingPlayerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(resolved))
                ));
                if (oncePerTurn) {
                    gameData.oncePerTurnTriggersFiredThisTurn.add(card.getId());
                }
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} triggers from the graveyard when its controller gains life",
                        gameData.id, card.getName());
            }
        }
    }

    public void processNextEnteringPermanentAnyTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextEnteringPermanentAnyTarget(gameData);
    }

    public void processNextSagaChapterTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSagaChapterTarget(gameData);
    }

    public void processNextSagaChapterPlayerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSagaChapterPlayerTarget(gameData);
    }

    public void processNextSagaChapterGraveyardTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextSagaChapterGraveyardTarget(gameData);
    }

    /** Fires triggers watching a Saga's final chapter ability after it has fully resolved. */
    public void checkSagaFinalChapterAbilityResolutionTriggers(GameData gameData,
                                                                UUID sagaControllerId, int sagaManaValue) {
        if (sagaControllerId == null) return;
        TriggerContext context = new TriggerContext.SagaFinalChapterAbilityResolved(
                sagaControllerId, sagaManaValue);
        gameData.forEachPermanent((controllerId, permanent) ->
                dispatchWatch(gameData, permanent, controllerId,
                        SagaWatch.ON_SAGA_FINAL_CHAPTER_ABILITY_RESOLVES, context));
    }

    /** Fires triggers watching a Saga's final chapter ability as it triggers (is put on the stack). */
    public void checkSagaFinalChapterAbilityTriggers(GameData gameData, UUID sagaControllerId, int sagaManaValue) {
        if (sagaControllerId == null) return;
        TriggerContext context = new TriggerContext.SagaFinalChapterAbilityResolved(
                sagaControllerId, sagaManaValue);
        gameData.forEachPermanent((controllerId, permanent) ->
                dispatchWatch(gameData, permanent, controllerId,
                        SagaWatch.ON_SAGA_FINAL_CHAPTER_ABILITY_TRIGGERS, context));
    }

    // ── Explore triggers ──────────────────────────────────────────────

    /**
     * Scans the exploring creature's controller's battlefield for permanents
     * with {@link ActionWatch#ON_ALLY_CREATURE_EXPLORES} effects and queues
     * them for target selection or directly onto the stack.
     */
    public void checkExploreTriggers(GameData gameData, UUID controllerId, Card exploredCard) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent perm : battlefield) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            List<CardEffect> registeredEffects = ActionWatch.ON_ALLY_CREATURE_EXPLORES.effectsOf(perm.getCard());
            if (registeredEffects == null || registeredEffects.isEmpty()) continue;

            List<CardEffect> effects = registeredEffects.stream()
                    .map(effect -> {
                        if (!(effect instanceof TriggeringCardConditionalEffect conditional)) {
                            return effect;
                        }
                        return exploredCard != null && predicateEvaluationService.matchesCardPredicate(
                                exploredCard, conditional.predicate(), perm.getCard().getId(), gameData, controllerId)
                                ? conditional.wrapped() : null;
                    })
                    .filter(Objects::nonNull)
                    .toList();
            if (effects.isEmpty()) continue;

            boolean anyTargeting = effects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
            if (anyTargeting) {
                gameData.queueInteraction(
                        new PermanentChoiceContext.ExploreTriggerTarget(
                                perm.getCard(), controllerId, new ArrayList<>(effects), perm.getId()));
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(effects),
                        null,
                        perm.getId()
                ));
            }
            log.info("Game {} - {} explore trigger queued", gameData.id, perm.getCard().getName());
        }
    }

    public void processNextExploreTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextExploreTriggerTarget(gameData);
    }

    /** Fires triggers for a creature controlled by a player conniving. */
    public void checkAllyCreatureConniveTriggers(GameData gameData, Permanent connivingCreature) {
        if (connivingCreature == null || !gameQueryService.isCreature(gameData, connivingCreature)) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, connivingCreature.getId());
        if (controllerId == null) {
            return;
        }
        checkAllyCreatureConniveTriggers(gameData, connivingCreature, controllerId);
    }

    /**
     * Fires the same triggers for a creature that connived using its last known information, with the
     * last known controller (CR 701.50b).
     */
    public void checkAllyCreatureConniveTriggers(GameData gameData, Permanent connivingCreature,
                                                 UUID controllerId) {
        if (connivingCreature == null || controllerId == null) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        TriggerContext context = new TriggerContext.CreatureConnives(connivingCreature, controllerId);
        for (Permanent watcher : List.copyOf(battlefield)) {
            dispatchWatch(gameData, watcher, controllerId, ActionWatch.ON_ALLY_CREATURE_CONNIVES, context);
        }
    }

    // ── Exploit triggers ──────────────────────────────────────────────

    /**
     * Queues this permanent's {@link ActionWatch#ON_EXPLOIT} ability after it successfully
     * exploited a creature. Uses the source card's LKI even if the permanent left the battlefield
     * (e.g. it sacrificed itself).
     */
    public void checkExploitTriggers(GameData gameData, Card sourceCard, UUID controllerId, UUID sourcePermanentId) {
        List<CardEffect> effects = ActionWatch.ON_EXPLOIT.effectsOf(sourceCard);
        if (effects == null || effects.isEmpty()) return;

        StackEntry originalEntry = gameData.pendingEffectResolutionEntry;

        boolean targetsStack = effects.stream().anyMatch(EffectResolution::targetsSpellOnStack);
        boolean targetsPermanent = effects.stream().anyMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
        boolean targetsPlayer = effects.stream().anyMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        boolean targetsGraveyard = effects.stream().anyMatch(effect ->
                effect.targetSpec().admits(com.github.laxika.magicalvibes.model.effect.TargetPredicate.Kind.GRAVEYARD_CARD));
        if (targetsGraveyard && !targetsStack && !targetsPermanent && !targetsPlayer) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    sourceCard, controllerId, new ArrayList<>(effects)));
        } else if (targetsStack) {
            StackEntryPredicate stackFilter = null;
            boolean includeAbilities = false;
            if (sourceCard.getTargetFilter() instanceof com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter sf) {
                stackFilter = sf.predicate();
                includeAbilities = predicateContainsHasTarget(sf.predicate());
            }
            gameData.queueInteraction(new PermanentChoiceContext.ExploitTriggerTarget(
                    sourceCard, controllerId, new ArrayList<>(effects), sourcePermanentId,
                    stackFilter, includeAbilities, sourceCard.getTargetFilter(),
                    originalEntry == null ? null : originalEntry.getSacrificedPermanentSnapshot(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedPower(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedColorCount(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedToughness()));
        } else if (targetsPermanent) {
            gameData.queueInteraction(new PermanentChoiceContext.ExploitPermanentTriggerTarget(
                    sourceCard, controllerId, new ArrayList<>(effects), sourcePermanentId,
                    originalEntry == null ? null : originalEntry.getSacrificedPermanentSnapshot(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedPower(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedColorCount(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedToughness()));
        } else if (targetsPlayer) {
            gameData.queueInteraction(new PermanentChoiceContext.ExploitPlayerTriggerTarget(
                    sourceCard, controllerId, new ArrayList<>(effects), sourcePermanentId,
                    sourceCard.getTargetFilter(),
                    originalEntry == null ? null : originalEntry.getSacrificedPermanentSnapshot(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedPower(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedColorCount(),
                    originalEntry == null ? 0 : originalEntry.getSacrificedToughness()));
        } else {
            StackEntry triggeredEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    controllerId,
                    sourceCard.getName() + "'s exploit ability",
                    new ArrayList<>(effects),
                    null,
                    sourcePermanentId
            );
            if (originalEntry != null) {
                triggeredEntry.setSacrificedPermanentSnapshot(originalEntry.getSacrificedPermanentSnapshot());
                triggeredEntry.setSacrificedPower(originalEntry.getSacrificedPower());
                triggeredEntry.setSacrificedColorCount(originalEntry.getSacrificedColorCount());
                triggeredEntry.setSacrificedToughness(originalEntry.getSacrificedToughness());
            }
            gameData.stack.add(triggeredEntry);
        }
        gameLogService.append(gameData,
                GameLog.cardThen(sourceCard, " exploits a creature."));
        log.info("Game {} - {} exploit trigger queued", gameData.id, sourceCard.getName());
    }

    public void processNextExploitTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextExploitTriggerTarget(gameData);
    }

    /**
     * Fires effects that watch a creature controlled by the exploiting player exploit a
     * creature. The exploiting permanent is also checked from its last-known battlefield object so
     * sacrificing it for its own exploit still lets its other triggered ability trigger.
     */
    public void checkAllyCreatureExploitTriggers(GameData gameData, UUID exploitingPlayerId,
                                                  Permanent exploitingPermanent, Card exploitedCard,
                                                  int exploitedPower) {
        if (exploitingPermanent == null || exploitedCard == null) return;

        TriggerContext ctx = new TriggerContext.CreatureExploit(
                exploitingPlayerId, exploitingPermanent.getCard(), exploitedCard, exploitedPower);
        List<Permanent> battlefield = gameData.playerBattlefields.get(exploitingPlayerId);
        boolean sourceStillPresent = false;
        if (battlefield != null) {
            for (Permanent perm : new ArrayList<>(battlefield)) {
                if (perm.getId().equals(exploitingPermanent.getId())) {
                    sourceStillPresent = true;
                }
                dispatchWatch(gameData, perm, exploitingPlayerId, ActionWatch.ON_ALLY_CREATURE_EXPLOITS, ctx);
                if (!exploitedCard.isToken()) {
                    dispatchWatch(gameData, perm, exploitingPlayerId,
                            ActionWatch.ON_ALLY_CREATURE_EXPLOITS_NONTOKEN_CREATURE, ctx);
                }
            }
        }

        if (!sourceStillPresent) {
            for (CardEffect effect : ActionWatch.ON_ALLY_CREATURE_EXPLOITS.effectsOf(exploitingPermanent.getCard())) {
                dispatch(new TriggerMatchContext(gameData, exploitingPermanent, exploitingPlayerId, effect),
                        ActionWatch.ON_ALLY_CREATURE_EXPLOITS, effect, ctx);
            }
            if (exploitedCard.isToken()) return;
            for (CardEffect effect : ActionWatch.ON_ALLY_CREATURE_EXPLOITS_NONTOKEN_CREATURE
                    .effectsOf(exploitingPermanent.getCard())) {
                dispatch(new TriggerMatchContext(gameData, exploitingPermanent, exploitingPlayerId, effect),
                        ActionWatch.ON_ALLY_CREATURE_EXPLOITS_NONTOKEN_CREATURE, effect, ctx);
            }
        }
    }

    public void processNextExploitPermanentTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextExploitPermanentTriggerTarget(gameData);
    }

    public void processNextExploitPlayerTriggerTarget(GameData gameData) {
        if (!gameData.hasPendingInteraction(PermanentChoiceContext.ExploitPlayerTriggerTarget.class)) {
            return;
        }

        PermanentChoiceContext.ExploitPlayerTriggerTarget trigger =
                gameData.pollPendingInteraction(PermanentChoiceContext.ExploitPlayerTriggerTarget.class);
        List<UUID> validPlayerTargets = validTargetService.filterValidPlayerTargets(
                gameData, trigger.targetFilter(), new ArrayList<>(gameData.orderedPlayerIds), trigger.controllerId());

        if (validPlayerTargets.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(trigger.sourceCard(), "'s exploit trigger has no valid targets."));
            log.info("Game {} - {} exploit player-target trigger skipped (no valid targets)",
                    gameData.id, trigger.sourceCard().getName());
            processNextExploitPlayerTriggerTarget(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(trigger);
        playerInputService.beginAnyTargetChoice(gameData, trigger.controllerId(), List.of(), validPlayerTargets,
                trigger.sourceCard().getName() + "'s exploit ability — Choose target player.");
        log.info("Game {} - {} exploit trigger awaiting player target selection",
                gameData.id, trigger.sourceCard().getName());
    }

    public static boolean predicateContainsHasTarget(com.github.laxika.magicalvibes.model.filter.StackEntryPredicate predicate) {
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate) {
            return true;
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate typeIn) {
            return typeIn.spellTypes().contains(StackEntryType.ACTIVATED_ABILITY)
                    || typeIn.spellTypes().contains(StackEntryType.TRIGGERED_ABILITY);
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate allOf) {
            return allOf.predicates().stream().anyMatch(TriggerCollectionService::predicateContainsHasTarget);
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryAnyOfPredicate anyOf) {
            return anyOf.predicates().stream().anyMatch(TriggerCollectionService::predicateContainsHasTarget);
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate not) {
            return predicateContainsHasTarget(not.predicate());
        }
        return false;
    }

    // ── Clash ──────────────────────────────────────────────────────────

    /**
     * Performs a clash for {@code clashingPlayerId} against their (single, 2-player) opponent
     * (MTG rule 701.29): both players reveal the top card of their library, the clashing player
     * wins if their revealed card's mana value is strictly greater than the opponent's, and then
     * {@link ActionWatch#ON_CONTROLLER_CLASHES} triggers on the clashing player's permanents.
     *
     * <p>The caller subsequently offers each player a top-or-bottom placement choice for their
     * revealed card before continuing the clash effect.
     * The "whenever you clash" triggers fire after the clash ends. Invoked from a clash-source card's
     * effect resolution (see {@code ClashEffect}) or a test.
     *
     * @return {@code true} if the clashing player won the clash (their revealed card had a strictly
     *         greater mana value), so callers can apply an "if you won" reward.
     */
    public boolean performClash(GameData gameData, UUID clashingPlayerId) {
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(clashingPlayerId))
                .findFirst().orElse(null);

        Card clashingCard = topCard(gameData, clashingPlayerId);
        Card opponentCard = opponentId != null ? topCard(gameData, opponentId) : null;

        GameLog.Builder clashLog = GameLog.builder()
                .text(gameData.playerIdToName.get(clashingPlayerId) + " clashes: reveals ");
        if (clashingCard != null) {
            clashLog.card(clashingCard);
        } else {
            clashLog.text("no card (empty library)");
        }
        clashLog.text("; opponent reveals ");
        if (opponentCard != null) {
            clashLog.card(opponentCard);
        } else {
            clashLog.text("no card (empty library)");
        }
        clashLog.text(".");
        gameLogService.append(gameData, clashLog.build());

        // 701.29c: win if your revealed card's mana value is higher than each other revealed card.
        boolean won = clashingCard != null
                && (opponentCard == null || clashingCard.getManaValue() > opponentCard.getManaValue());

        String outcome = won
                ? gameData.playerIdToName.get(clashingPlayerId) + " won the clash."
                : "No one won the clash.";
        gameLogService.append(gameData, GameLog.text(outcome));
        log.info("Game {} - {} clashes (won={})", gameData.id, clashingPlayerId, won);

        fireClashTriggers(gameData, clashingPlayerId, won);
        if (opponentId != null) {
            boolean opponentWon = opponentCard != null
                    && (clashingCard == null || opponentCard.getManaValue() > clashingCard.getManaValue());
            fireClashTriggers(gameData, opponentId, opponentWon);
        }
        return won;
    }

    private Card topCard(GameData gameData, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        return (deck == null || deck.isEmpty()) ? null : deck.getFirst();
    }

    private void fireClashTriggers(GameData gameData, UUID clashingPlayerId, boolean won) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(clashingPlayerId);
        if (battlefield == null) return;

        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
            List<CardEffect> effects = ActionWatch.ON_CONTROLLER_CLASHES.effectsOf(perm.getCard());
            if (effects == null || effects.isEmpty()) continue;

            // Resolve win-conditional clauses now: the clash has ended, so the winner is fixed.
            List<CardEffect> resolvedEffects = new ArrayList<>();
            for (CardEffect effect : effects) {
                if (effect instanceof ClashOutcomeConditionalEffect clashOutcome) {
                    if (clashOutcome.appliesOnWin() == won) resolvedEffects.add(clashOutcome.wrapped());
                } else {
                    resolvedEffects.add(effect);
                }
            }
            if (resolvedEffects.isEmpty()) continue;

            // Targeting clash triggers (Entangling Trap) route through the ClashTriggerTarget
            // interaction to pick an opponent's creature; non-targeting ones (Rebellion of the
            // Flamekin) go straight onto the stack as a triggered ability.
            boolean needsTarget = resolvedEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT) || e.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            if (needsTarget) {
                gameData.queueInteraction(new PermanentChoiceContext.ClashTriggerTarget(
                        perm.getCard(), clashingPlayerId, resolvedEffects, perm.getId()));
                log.info("Game {} - {} clash trigger queued", gameData.id, perm.getCard().getName());
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        clashingPlayerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(resolvedEffects),
                        null,
                        perm.getId()));
                log.info("Game {} - {} clash trigger pushed to stack", gameData.id, perm.getCard().getName());
            }
        }

        triggeredAbilityQueueService.processNextClashTriggerTarget(gameData);
    }

    public void processNextClashTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextClashTriggerTarget(gameData);
    }

    // ── Death / leaves-battlefield triggers ───────────────────────────

    /**
     * {@code effect}, a "when this dies" effect of {@code dyingCard}, bound to the card that died, the Auras and
     * Equipment that were attached to it and its graveyard incarnation (CR 400.7, CR 603.10a).
     */
    public CardEffect bindSelfDeathEffect(GameData gameData, Card dyingCard, Permanent dyingPermanent,
                                          DepartedAttachments attachments, CardEffect effect) {
        UUID dyingCardId = dyingPermanent != null ? dyingPermanent.getOriginalCard().getId() : dyingCard.getId();
        CardEffect bound = effect;
        if (bound instanceof DyingCreatureCardAwareEffect aware) {
            bound = bindDyingCard(gameData, aware, dyingCardId);
        }
        if (dyingPermanent != null) {
            bound = bindDyingCreatureAttachments(bound, DyingCreatureAttachmentSnapshot.of(attachments));
        }
        return bindSourceCardInGraveyardVersion(gameData, bound, dyingCardId);
    }

    /**
     * Puts one "whenever [an object] dies" ability of {@code perm} on the stack for {@code death}, binding the event
     * data its effects need the way the old slot collector of {@code watch} did. The trigger pipeline has already
     * decided that the ability triggers: its relation, its filter, the zone it functions from and the look-back of
     * the event (CR 603.10a). {@code perm} is the source as the event sees it: on the battlefield, its last-known
     * copy if it left in the same event, or a stand-in for a card in a graveyard or in exile. The pipeline has also
     * checked the ability's limit and its intervening "if"; {@code limit} says whether accepting a "you may" uses up
     * "do this only once each turn" (CR 603.2h) and whether an ability that triggers only once each turn may be
     * made to trigger an additional time.
     *
     * @return whether anything was put on the stack or queued for it
     */
    public boolean triggerCreatureDeath(GameData gameData, CreatureDeathWatch watch, Permanent perm,
                                        UUID controllerId, List<CardEffect> effects, TriggerLimit limit,
                                        CreatureDeath death) {
        return switch (watch) {
            case ALLY -> collectAllyCreatureDeath(gameData, perm, controllerId, effects, limit, death);
            case ANY -> collectAnyCreatureDeath(gameData, perm, controllerId, effects, death);
            case OPPONENT -> collectOpponentCreatureDeath(gameData, perm, controllerId, effects, limit, death);
            case ALLY_NONTOKEN, ANY_NONTOKEN, ENCHANTED_PLAYER_NONTOKEN ->
                    collectNontokenCreatureDeath(gameData, watch, perm, controllerId, effects, death);
            case ALLY_CREATURE_OR_PLANESWALKER ->
                    collectAllyCreatureOrPlaneswalkerDeath(gameData, perm, controllerId, effects, death);
            case EQUIPPED -> collectEquippedCreatureDeath(gameData, perm, controllerId, effects, death);
            case DAMAGED -> collectDamagedCreatureDeath(gameData, perm, controllerId, effects, death);
            case HAUNTED -> collectHauntedCreatureDeath(gameData, perm.getCard(), controllerId, effects);
            case GRAVEYARD_ALLY -> collectGraveyardAllyCreatureDeath(gameData, perm.getCard(), controllerId,
                    effects, death);
            case GRAVEYARD_OPPONENT -> collectGraveyardOpponentCreatureDeath(gameData, perm.getCard(), controllerId,
                    effects, death);
            case ALLY_ARTIFACT_OR_CREATURE ->
                    triggerCreatureDeaths(gameData, watch, perm, controllerId, effects, limit, List.of(death));
        };
    }

    /**
     * Puts a "whenever one or more [objects] die" ability of {@code perm} on the stack once for {@code deaths}, the
     * deaths of one event that met its trigger condition, in the order they happened (CR 603.2c). The first death
     * is the one its effects bind.
     *
     * @return whether anything was put on the stack or queued for it
     */
    public boolean triggerCreatureDeaths(GameData gameData, CreatureDeathWatch watch, Permanent perm,
                                         UUID controllerId, List<CardEffect> effects, TriggerLimit limit,
                                         List<CreatureDeath> deaths) {
        return switch (watch) {
            case GRAVEYARD_ALLY, GRAVEYARD_OPPONENT ->
                    collectGraveyardCreatureDeaths(gameData, perm.getCard(), controllerId, effects, deaths.getFirst());
            case ALLY_ARTIFACT_OR_CREATURE -> collectArtifactOrCreatureDeaths(gameData, perm, controllerId, effects,
                    deaths);
            case ANY -> collectAnyCreatureDeaths(gameData, perm, controllerId, effects, deaths);
            default -> collectAllyCreatureDeaths(gameData, watch, perm, controllerId, effects, limit, deaths);
        };
    }

    private boolean collectAllyCreatureDeath(GameData gameData, Permanent perm, UUID controllerId,
                                             List<CardEffect> effects, TriggerLimit limit, CreatureDeath death) {
        Permanent dyingPermanent = death.permanent();
        Card dyingCard = dyingPermanent.getCard();
        int dyingPowerAtDeath = death.power();
        int dyingToughnessAtDeath = death.toughness();
        int dyingPower = Math.max(0, dyingPowerAtDeath);
        Map<CounterType, Integer> dyingCounters = snapshotCountersOnPermanent(dyingPermanent);
        var ctx = new TriggerContext.CreatureDeath(dyingCard, death.controllerId(),
                dyingPower, dyingToughnessAtDeath, dyingPermanent.getId(), dyingPermanent);

        boolean anyEffectFired = false;
        boolean markOnAcceptance = limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        List<CardEffect> stackEffects = new ArrayList<>();

        for (CardEffect effect : effects) {
            CardEffect resolvedEffect = effect;
            if (!passesInterveningIf(gameData, perm, controllerId, resolvedEffect,
                    dyingPermanent.getId(), dyingPower)) continue;
            resolvedEffect = snapshotDyingPermanentManaValue(gameData, controllerId, resolvedEffect, dyingPermanent);
            if (resolvedEffect instanceof DyingCreatureCountersAwareEffect aware) {
                resolvedEffect = aware.boundToDyingCreatureCounters(dyingCounters);
            }
            if (resolvedEffect instanceof DyingCreatureCardAwareEffect aware) {
                resolvedEffect = bindDyingCard(gameData, aware, dyingCard);
            }
            if (resolvedEffect instanceof DyingCreatureAttachmentsAwareEffect aware) {
                resolvedEffect = aware.boundToDyingCreatureAttachments(
                        death.attachments().auraCardIds(), death.attachments().equipmentPermanentIds());
            }
            if (resolvedEffect instanceof GainLifeEqualToDyingCreatureToughnessEffect) {
                resolvedEffect = new GainLifeEffect(Math.max(0, dyingToughnessAtDeath));
            }

            if (resolvedEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || resolvedEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || resolvedEffect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                // Targeted "another creature you control dies" trigger (e.g. Diregraf Captain): the controller
                // picks a target as the ability is put on the stack (CR 603.3d). The source card here is the
                // watching permanent, so its own target filter (e.g. opponent-only) is honoured. The dying
                // creature's last-known power rides along as the event value, so an amount declared as
                // EventValue resolves to it (Death's Presence).
                // The card that went to the graveyard is left out ("another target creature card"): for a copy, the
                // physical card, not the card it copied.
                gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                        perm.getCard(), controllerId, new ArrayList<>(List.of(resolvedEffect)),
                        dyingPower, new Permanent(perm), null, dyingPermanent.getOriginalCard().getId(), true,
                        markOnAcceptance
                ));
                anyEffectFired = true;
            } else if (resolvedEffect instanceof MayPayManaEffect
                    || resolvedEffect instanceof MayPayLifeEffect
                    || resolvedEffect instanceof MayEffect) {
                var match = new TriggerMatchContext(gameData, perm, controllerId, resolvedEffect, perm.getCard(),
                        markOnAcceptance);
                dispatchCreatureDeath(match, CreatureDeathWatch.ALLY, resolvedEffect, ctx);
                anyEffectFired = true;
            } else {
                if (resolvedEffect instanceof ReturnTriggeringCardToOwnerHandEffect returnToOwner
                        && returnToOwner.handOwnerId() == null) {
                    UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCard.getId());
                    if (ownerId != null) {
                        resolvedEffect = new ReturnTriggeringCardToOwnerHandEffect(dyingCard.getId(), ownerId);
                    }
                }
                if (resolvedEffect instanceof DyingCreatureCounterAwareEffect aware) {
                    resolvedEffect = aware.boundToDyingCreatureCounterCount(countCountersOnPermanent(dyingPermanent));
                }
                stackEffects.add(resolvedEffect);
            }
        }

        if (!stackEffects.isEmpty()) {
            List<CardEffect> entryEffects = stackEffects;
            Card watcherCard = perm.getOriginalCard() != null
                    && perm.getOriginalCard().getId().equals(perm.getCard().getId())
                    && perm.getOriginalCard().getName().equals(perm.getCard().getName())
                    ? perm.getOriginalCard() : perm.getCard();
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcherCard,
                    controllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(entryEffects),
                    null,
                    perm.getId()
            );
            entry.setSourcePermanentSnapshot(new Permanent(perm));
            entry.setTriggeringPermanentId(dyingPermanent.getId());
            entry.rememberLastKnownPermanentCard(dyingPermanent.getId(), dyingCard);
            // A comparison against another creature's power uses the dying creature's actual last
            // known power, which can be negative (CR 208.3); only damage amounts treat it as 0.
            boolean comparesPower = entryEffects.stream().anyMatch(
                    e -> e instanceof ConditionalEffect c
                            && c.condition() instanceof TriggeringPermanentPowerGreaterThanSourcePower);
            entry.setTriggeringPermanentPowerAtTrigger(comparesPower ? dyingPowerAtDeath : dyingPower);
            entry.setTriggeringCardId(dyingCard.getId());
            entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(dyingCard.getId()));
            // A mana value bound compares against the creature's actual last known power, which can be
            // negative (CR 208.3), so an unclamped power excludes even mana value 0.
            boolean powerBoundsManaValue = entryEffects.stream().anyMatch(
                    e -> e instanceof PutCardToBattlefieldEffect put && put.maxManaValueBoundedByEventValue());
            entry.setEventValue(powerBoundsManaValue ? dyingPowerAtDeath : dyingPower);
            // "This ability triggers only once each turn" can't be made to trigger an additional time.
            int additionalCopies = limit == TriggerLimit.ONCE_EACH_TURN ? 0
                    : gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggers(
                            gameData, controllerId, perm);
            int previousCopies = gameData.beginTriggeredAbilityCopies(1 + additionalCopies);
            try {
                gameData.stack.add(entry);
            } finally {
                gameData.restoreTriggeredAbilityCopies(previousCopies);
            }
            anyEffectFired = true;
        }

        if (anyEffectFired) {
            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            log.info("Game {} - {} triggers (ally creature died)", gameData.id, perm.getCard().getName());
        }
        return anyEffectFired;
    }

    private boolean collectAnyCreatureDeath(GameData gameData, Permanent perm, UUID playerId,
                                            List<CardEffect> effects, CreatureDeath death) {
        Permanent dyingPermanent = death.permanent();
        Card dyingCard = dyingPermanent.getCard();
        var ctx = new TriggerContext.CreatureDeath(dyingCard, death.controllerId(),
                death.power(), death.toughness(), dyingPermanent.getId(), dyingPermanent);
        boolean triggered = false;
        for (CardEffect effect : effects) {
            CardEffect resolvedEffect = effect;
            if (resolvedEffect instanceof DyingCreatureCardAwareEffect aware) {
                resolvedEffect = bindDyingCard(gameData, aware, dyingCard);
            }
            var match = new TriggerMatchContext(gameData, perm, playerId, resolvedEffect);
            if (dispatchCreatureDeath(match, CreatureDeathWatch.ANY, resolvedEffect, ctx)) {
                triggered = true;
            }
        }
        return triggered;
    }

    private boolean collectOpponentCreatureDeath(GameData gameData, Permanent perm, UUID playerId,
                                                 List<CardEffect> effects, TriggerLimit limit, CreatureDeath death) {
        Permanent dyingPermanent = death.permanent();
        Card dyingCard = dyingPermanent.getCard();
        UUID dyingCreatureControllerId = death.controllerId();
        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingCreatureControllerId,
                death.power(), death.toughness(), dyingPermanent.getId(), dyingPermanent);
        boolean triggered = false;
        boolean markOnAcceptance = limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        for (CardEffect effect : effects) {
            CardEffect resolvedEffect = effect;
            if (resolvedEffect instanceof DyingCreatureCardAwareEffect aware) {
                resolvedEffect = bindDyingCard(gameData, aware, dyingCard);
            }
            var match = new TriggerMatchContext(gameData, perm, playerId, resolvedEffect, perm.getCard(),
                    markOnAcceptance);
            if (dispatchCreatureDeath(match, CreatureDeathWatch.OPPONENT, resolvedEffect, ctx)) {
                triggered = true;
            }
        }
        return triggered;
    }

    /**
     * "A nontoken creature you control", "a nontoken creature" and a Curse's "a nontoken creature enchanted player
     * controls": each effect through its handler, read with the permanent's text changes (layer 3, CR 612.1).
     */
    private boolean collectNontokenCreatureDeath(GameData gameData, CreatureDeathWatch watch, Permanent perm,
                                                 UUID controllerId, List<CardEffect> effects, CreatureDeath death) {
        Permanent dyingPermanent = death.permanent();
        Card dyingCard = dyingPermanent.getCard();
        TriggerContext ctx = watch == CreatureDeathWatch.ANY_NONTOKEN
                ? new TriggerContext.CreatureDeath(dyingCard, death.ownerId(),
                        dyingCard.getPower() != null ? dyingCard.getPower() : 0,
                        dyingCard.getToughness() != null ? dyingCard.getToughness() : 0)
                : new TriggerContext.CreatureDeath(dyingCard, death.controllerId(), death.power(),
                        dyingPermanent.getEffectiveToughness(), dyingPermanent.getId(), dyingPermanent);
        List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
        boolean triggered = false;
        for (CardEffect effect : effects) {
            CardEffect transformed = TextChangeTransformer.transform(
                    effect, perm.getTextReplacements(), globalWordChange);
            if (dispatchCreatureDeathEffect(gameData, perm, controllerId, watch, ctx, transformed)) {
                triggered = true;
            }
        }
        return triggered;
    }

    /** One effect of a creature-death ability through its handler, as a slot collector dispatched one. */
    private boolean dispatchCreatureDeathEffect(GameData gameData, Permanent perm, UUID controllerId,
                                                CreatureDeathWatch watch, TriggerContext ctx, CardEffect effect) {
        return dispatchCreatureDeath(new TriggerMatchContext(gameData, perm, controllerId, effect), watch, effect,
                ctx);
    }

    private boolean collectAllyCreatureOrPlaneswalkerDeath(GameData gameData, Permanent perm, UUID controllerId,
                                                           List<CardEffect> effects, CreatureDeath death) {
        Permanent dyingPermanent = death.permanent();
        Card dyingCard = dyingPermanent.getCard();
        boolean wasCreature = dyingCard.hasType(CardType.CREATURE);
        var ctx = new TriggerContext.CreatureDeath(dyingCard, death.controllerId(),
                death.power(), death.toughness(), dyingPermanent.getId(), dyingPermanent, wasCreature);
        boolean anyEffectFired = false;
        List<CardEffect> stackEffects = new ArrayList<>();
        for (CardEffect effect : effects) {
            CardEffect resolvedEffect = effect;
            if (resolvedEffect instanceof MayPayManaEffect
                    || resolvedEffect instanceof MayPayLifeEffect
                    || resolvedEffect instanceof MayEffect) {
                var match = new TriggerMatchContext(gameData, perm, controllerId, resolvedEffect);
                dispatchCreatureDeath(match, CreatureDeathWatch.ALLY_CREATURE_OR_PLANESWALKER, resolvedEffect, ctx);
                anyEffectFired = true;
            } else {
                stackEffects.add(resolvedEffect);
            }
        }

        if (!stackEffects.isEmpty()) {
            int previousCopies = gameData.beginTriggeredAbilityCopies(1
                    + (wasCreature
                    ? gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggers(
                            gameData, controllerId, perm)
                    : 0));
            try {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(stackEffects),
                        null,
                        perm.getId()
                ));
            } finally {
                gameData.restoreTriggeredAbilityCopies(previousCopies);
            }
            anyEffectFired = true;
        }

        if (anyEffectFired) {
            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            log.info("Game {} - {} triggers (ally creature or planeswalker died)", gameData.id,
                    perm.getCard().getName());
        }
        return anyEffectFired;
    }

    private boolean collectEquippedCreatureDeath(GameData gameData, Permanent perm, UUID equipmentControllerId,
                                                 List<CardEffect> effects, CreatureDeath death) {
        Permanent dyingPermanent = death.permanent();
        // The card it died as holds the creature types it had (CR 603.10a), for "if it was a Human".
        Card lastKnownConditionalCard = dyingPermanent.getCard();
        Card dyingCard = death.card();
        var ctx = new TriggerContext.EquippedCreatureDeath(
                dyingPermanent.getId(), death.controllerId(), dyingCard, death.power(), new Permanent(dyingPermanent));
        boolean triggered = false;
        for (CardEffect effect : effects) {
            // "if it's a Samurai card" (Oathkeeper) checks the printed card, not subtypes granted to the creature.
            Card conditionalCardForEffect = effect instanceof TriggeringCardConditionalEffect conditional
                    && conditional.wrapped() instanceof ReturnDyingCreatureToBattlefieldEffect
                    ? dyingCard
                    : lastKnownConditionalCard;
            CardEffect resolvedEffect = unwrapTriggeringCardConditional(effect, conditionalCardForEffect,
                    gameData, equipmentControllerId);
            if (resolvedEffect == null) continue;
            var match = new TriggerMatchContext(gameData, perm, equipmentControllerId, resolvedEffect);
            if (dispatchCreatureDeath(match, CreatureDeathWatch.EQUIPPED, resolvedEffect, ctx)) {
                triggered = true;
            }
        }
        return triggered;
    }

    /**
     * "Whenever a creature dealt damage by this creature (or by equipped or enchanted creature) this turn dies",
     * with the dying creature's last-known information bound into the effects that need it: its toughness, its
     * card for a "may exile that card", its controller for "its controller loses N life".
     */
    private boolean collectDamagedCreatureDeath(GameData gameData, Permanent sourcePermanent, UUID controllerId,
                                                List<CardEffect> effects, CreatureDeath death) {
        Card sourceCard = sourcePermanent.getCard();
        UUID sourcePermanentId = sourcePermanent.getId();
        Permanent dyingPermanent = death.permanent();
        Card dyingCreatureCard = death.card();
        UUID dyingControllerId = death.controllerId();
        UUID dyingCreatureCardId = dyingCreatureCard.getId();
        boolean triggered = false;
        for (CardEffect effect : effects) {
            CardEffect materializedEffect = materializeDamagedCreatureDiesEffect(effect, dyingCreatureCard,
                    dyingPermanent);
            triggered = true;
            if (materializedEffect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || materializedEffect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || materializedEffect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                        sourceCard, controllerId, List.of(materializedEffect),
                        "damaged creature dies", sourcePermanentId));
                continue;
            }
            StackEntry triggerEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    controllerId,
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(materializedEffect)),
                    null,
                    sourcePermanentId
            );
            // The dying creature's card id as last-known information, for effects that act on it
            // (e.g. Seraph returns "that card" at the next end step).
            triggerEntry.setTriggeringCardId(dyingCreatureCardId);
            // The graveyard visit this trigger refers to (CR 400.7): a later effect that acts on "that card"
            // must not act on the same card after it has left and returned to a graveyard again.
            triggerEntry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(dyingCreatureCardId));
            // "its controller loses N life" reads the dying creature's last-known controller from
            // the entry's targetId; the ability itself chooses no target. Other abilities use targetId
            // for their own purposes, so it is only bound for that recipient.
            if (materializedEffect instanceof LoseLifeEffect lose
                    && lose.recipient() == LoseLifeRecipient.DYING_CREATURE_CONTROLLER) {
                triggerEntry.setTargetId(dyingControllerId);
            }
            gameData.stack.add(triggerEntry);
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} triggers (damaged creature died this turn)", gameData.id, sourceCard.getName());
        }
        return triggered;
    }

    /**
     * Binds the dying creature's last-known information into effects that need it. The "you may exile
     * that card" ability (Unscythe) loses the stack entry's triggering-card id once wrapped as a may
     * ability, so the dying card id is bound onto the wrapped effect here instead.
     */
    private static CardEffect materializeDamagedCreatureDiesEffect(CardEffect effect, Card dyingCreatureCard,
                                                                   Permanent dyingPermanent) {
        if (effect instanceof GainLifeEqualToToughnessEffect) {
            int toughness = dyingPermanent.getLastKnownToughness() != null
                    ? dyingPermanent.getLastKnownToughness() : dyingPermanent.getEffectiveToughness();
            return new GainLifeEffect(Math.max(0, toughness));
        }
        if (effect instanceof MayEffect may && may.wrapped() instanceof DyingCreatureCardAwareEffect aware) {
            return new MayEffect(aware.boundToDyingCard(dyingCreatureCard.getId()), may.prompt());
        }
        if (effect instanceof DyingCreatureCardAwareEffect aware) {
            return aware.boundToDyingCard(dyingCreatureCard.getId());
        }
        return effect;
    }

    /**
     * "When the creature it haunts dies", from exile (CR 702.55c). A targeted ability is one triggered ability
     * (CR 603.3d): its other effects are skipped with it if no target is legal.
     */
    private boolean collectHauntedCreatureDeath(GameData gameData, Card hauntingCard, UUID ownerId,
                                                List<CardEffect> effects) {
        List<CardEffect> targetedEffects = new ArrayList<>();
        List<CardEffect> nonTargetedEffects = new ArrayList<>();
        List<TriggeredModalEffect> triggeredModalEffects = new ArrayList<>();
        for (CardEffect effect : effects) {
            if (effect instanceof TriggeredModalEffect triggeredModalEffect) {
                triggeredModalEffects.add(triggeredModalEffect);
                continue;
            }
            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                    || graveyardTargetingSupport.findTarget(List.of(effect)) != null) {
                targetedEffects.add(effect);
            } else {
                nonTargetedEffects.add(effect);
            }
        }
        if (!targetedEffects.isEmpty()) {
            List<CardEffect> abilityEffects = effects.stream()
                    .filter(effect -> !(effect instanceof TriggeredModalEffect))
                    .toList();
            gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    hauntingCard, ownerId, abilityEffects));
        } else if (!nonTargetedEffects.isEmpty()) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    hauntingCard,
                    ownerId,
                    hauntingCard.getName() + "'s ability",
                    nonTargetedEffects,
                    null));
            gameLogService.append(gameData, GameLog.abilityTriggers(hauntingCard));
        }
        for (TriggeredModalEffect triggeredModalEffect : triggeredModalEffects) {
            gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                    hauntingCard, ownerId, triggeredModalEffect.choice(), null));
            gameLogService.append(gameData, GameLog.abilityTriggers(hauntingCard));
        }
        return true;
    }

    private boolean collectGraveyardAllyCreatureDeath(GameData gameData, Card card, UUID controllerId,
                                                      List<CardEffect> effects, CreatureDeath death) {
        Card dyingCard = death.dyingCard();
        boolean triggered = false;
        for (CardEffect effect : effects) {
            CardEffect resolved = unwrapTriggeringCardConditional(
                    effect, dyingCard, gameData, controllerId, card.getId());
            if (resolved == null) continue;

            if (resolved instanceof MayEffect may) {
                gameData.queueMayAbility(card, controllerId, may, card.getId());
            } else {
                StackEntry triggerEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(resolved))
                );
                // CR 400.7: the ability refers to this graveyard object, not a later one.
                triggerEntry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                gameData.stack.add(triggerEntry);
            }
            triggered = true;
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
            log.info("Game {} - {} graveyard creature-death trigger queued", gameData.id, card.getName());
        }
        return triggered;
    }

    private boolean collectGraveyardOpponentCreatureDeath(GameData gameData, Card sourceCard, UUID controllerId,
                                                          List<CardEffect> effects, CreatureDeath death) {
        boolean triggered = false;
        for (CardEffect effect : effects) {
            queueGraveyardCreatureDeathTrigger(gameData, sourceCard, controllerId,
                    effect, death.dyingCard(), death.controllerId());
            triggered = true;
        }
        return triggered;
    }

    /** "Whenever one or more [other] creatures die" from a graveyard: one stack object for its plain effects. */
    private boolean collectGraveyardCreatureDeaths(GameData gameData, Card sourceCard, UUID controllerId,
                                                   List<CardEffect> effects, CreatureDeath firstDeath) {
        boolean triggered = false;
        List<CardEffect> stackEffects = new ArrayList<>();
        for (CardEffect resolved : effects) {
            if (resolved instanceof MayEffect may) {
                gameData.queueMayAbility(sourceCard, controllerId, may);
                triggered = true;
            } else {
                stackEffects.add(resolved);
            }
        }
        if (stackEffects.isEmpty()) return triggered;

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                stackEffects));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        return true;
    }

    /** "Whenever one or more artifacts and/or creatures you control are put into a graveyard from the battlefield". */
    private boolean collectArtifactOrCreatureDeaths(GameData gameData, Permanent perm, UUID controllerId,
                                                    List<CardEffect> effects, List<CreatureDeath> deaths) {
        CreatureDeath firstDeath = deaths.getFirst();
        Permanent dyingPermanent = firstDeath.permanent();
        Card dyingCard = dyingPermanent.getCard();
        boolean eventContainsCreature = deaths.stream()
                .anyMatch(death -> death.dyingCard().hasType(CardType.CREATURE));
        TriggerContext context = new TriggerContext.ArtifactOrCreatureDeath(
                dyingCard, controllerId,
                gameQueryService.findGraveyardOwnerById(gameData, dyingCard.getId()),
                dyingPermanent, firstDeath.power(), firstDeath.toughness(), eventContainsCreature);
        boolean triggered = false;
        for (CardEffect effect : effects) {
            if (dispatchCreatureDeathEffect(gameData, perm, controllerId,
                    CreatureDeathWatch.ALLY_ARTIFACT_OR_CREATURE, context, effect)) {
                triggered = true;
            }
        }
        return triggered;
    }

    /** "Whenever one or more [other] creatures die": one stack object for its plain effects. */
    private boolean collectAnyCreatureDeaths(GameData gameData, Permanent watcher, UUID watcherControllerId,
                                             List<CardEffect> effects, List<CreatureDeath> deaths) {
        CreatureDeath firstDeath = deaths.getFirst();
        Permanent dyingPermanent = firstDeath.permanent();
        Card dyingCard = dyingPermanent.getCard();
        int dyingPower = Math.max(0, firstDeath.power());
        TriggerContext context = new TriggerContext.CreatureDeath(dyingCard, firstDeath.controllerId(),
                dyingPower, firstDeath.toughness(), dyingPermanent.getId(), dyingPermanent);
        boolean triggered = false;
        List<CardEffect> stackEffects = new ArrayList<>();
        for (CardEffect effect : effects) {
            CardEffect resolved = effect;
            if (!passesInterveningIf(gameData, watcher, watcherControllerId, resolved,
                    dyingPermanent.getId(), dyingPower)) continue;

            if (resolved.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || resolved.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                        watcher.getCard(), watcherControllerId, new ArrayList<>(List.of(resolved)),
                        dyingPower, new Permanent(watcher), dyingCard.getId()));
                triggered = true;
            } else if (resolved instanceof MayPayManaEffect || resolved instanceof MayEffect) {
                dispatchCreatureDeath(new TriggerMatchContext(gameData, watcher, watcherControllerId, resolved),
                        CreatureDeathWatch.ANY, resolved, context);
                triggered = true;
            } else {
                if (resolved instanceof DyingCreatureCardAwareEffect aware) {
                    resolved = bindDyingCard(gameData, aware, dyingCard);
                }
                stackEffects.add(resolved);
            }
        }
        if (stackEffects.isEmpty()) return triggered;

        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, watcher.getCard(),
                watcherControllerId, watcher.getCard().getName() + "'s ability", stackEffects,
                null, watcher.getId());
        entry.setSourcePermanentSnapshot(new Permanent(watcher));
        entry.setTriggeringPermanentId(dyingPermanent.getId());
        entry.setTriggeringPermanentPowerAtTrigger(dyingPower);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
        return true;
    }

    /**
     * "Whenever one or more [other] creatures you control die": one stack object for its plain effects, with the
     * cards of every death that met its trigger condition, for "return those cards".
     */
    private boolean collectAllyCreatureDeaths(GameData gameData, CreatureDeathWatch watch, Permanent watcher,
                                              UUID watcherControllerId, List<CardEffect> effects,
                                              TriggerLimit limit, List<CreatureDeath> deaths) {
        CreatureDeath firstDeath = deaths.getFirst();
        Permanent dyingPermanent = firstDeath.permanent();
        Card dyingCard = dyingPermanent.getCard();
        int dyingPower = Math.max(0, firstDeath.power());
        TriggerContext context = new TriggerContext.CreatureDeath(dyingCard, watcherControllerId,
                dyingPower, firstDeath.toughness(), dyingPermanent.getId(), dyingPermanent);
        List<UUID> eventCardIds = deaths.stream().map(death -> death.dyingCard().getId()).toList();
        boolean triggered = false;
        List<CardEffect> stackEffects = new ArrayList<>();
        boolean markOnAcceptance = limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        for (CardEffect effect : effects) {
            CardEffect resolved = effect;
            if (!passesInterveningIf(gameData, watcher, watcherControllerId, resolved,
                    dyingPermanent.getId(), dyingPower)) continue;
            if (resolved.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                        watcher.getCard(), watcherControllerId, new ArrayList<>(List.of(resolved)), dyingPower,
                        null, null, null, true, markOnAcceptance));
                triggered = true;
            } else if (resolved instanceof MayPayManaEffect
                    || resolved instanceof MayPayLifeEffect
                    || resolved instanceof MayEffect) {
                dispatchCreatureDeath(new TriggerMatchContext(gameData, watcher, watcherControllerId, resolved,
                        watcher.getCard(), markOnAcceptance), watch, resolved, context);
                triggered = true;
            } else {
                if (resolved instanceof DyingCreatureCardAwareEffect aware) {
                    resolved = bindDyingCard(gameData, aware, dyingCard);
                }
                if (resolved instanceof DyingCreatureCounterAwareEffect aware) {
                    resolved = aware.boundToDyingCreatureCounterCount(countCountersOnPermanent(dyingPermanent));
                }
                stackEffects.add(resolved);
            }
        }
        if (stackEffects.isEmpty()) return triggered;
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, watcher.getCard(),
                watcherControllerId, watcher.getCard().getName() + "'s ability",
                stackEffects, null, watcher.getId());
        entry.setSourcePermanentSnapshot(new Permanent(watcher));
        entry.setTriggeringPermanentId(dyingPermanent.getId());
        entry.setTriggeringPermanentPowerAtTrigger(dyingPower);
        entry.setEventCardIds(eventCardIds);
        gameData.stack.add(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
        return true;
    }

    /**
     * The creature-death triggered abilities that aren't abilities of an object the trigger pipeline indexes: the
     * global ones a resolved spell or ability registered (CR 603.7), emblems', the delayed "when that creature
     * dies" watchers and face-up planes' (CR 901.7). They trigger as {@code dyingPermanent}, which
     * {@code dyingControllerId} controlled, dies.
     */
    public void checkCreatureDeathRegisteredTriggers(GameData gameData, UUID dyingControllerId,
                                                     Permanent dyingPermanent, int dyingPowerAtDeath,
                                                     int dyingToughnessAtDeath) {
        Card dyingCard = dyingPermanent.getCard();
        var ctx = new TriggerContext.CreatureDeath(dyingCard, dyingControllerId,
                dyingPowerAtDeath, dyingToughnessAtDeath, dyingPermanent.getId(), dyingPermanent);
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            collectRegisteredCreatureDeathTrigger(gameData, watcher, dyingControllerId, dyingPermanent,
                    dyingPowerAtDeath, dyingToughnessAtDeath);
        }
        collectEmblemCreatureDeathTriggers(gameData, dyingCard, ctx);
        collectCreatureDeathTriggerWatchers(gameData, dyingPermanent);
        collectPlanarCreatureDeathTriggers(gameData, dyingCard, dyingPermanent, dyingControllerId);
    }

    private void collectRegisteredCreatureDeathTrigger(GameData gameData, TemporaryGlobalTriggeredAbility watcher,
                                                       UUID dyingControllerId, Permanent dyingPermanent,
                                                       int dyingPowerAtDeath, int dyingToughnessAtDeath) {
        ZoneChangeTrigger trigger = creatureDeathTrigger(watcher.ability());
        if (trigger == null) {
            return;
        }
        Card dyingCard = dyingPermanent.getCard();
        if (watcher.watchedPermanentId() != null) {
            // "When it dies this turn": a delayed trigger watching one creature (Desperate Measures).
            if (trigger.relation() != ObjectRelation.THIS
                    || !dyingPermanent.getId().equals(watcher.watchedPermanentId())) {
                return;
            }
            CardEffect effect = watcher.effect();
            if (effect instanceof TriggeringPermanentControllerConditionalEffect conditional) {
                if (!watcher.controllerId().equals(dyingControllerId)) return;
                effect = conditional.wrapped();
            }
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, watcher.sourceCard(),
                    watcher.controllerId(), watcher.sourceCard().getName() + "'s ability", List.of(effect));
            entry.setTargetId(dyingControllerId);
            entry.setTriggeringPermanentId(dyingPermanent.getId());
            entry.setTriggeringPermanentControllerId(dyingControllerId);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            return;
        }
        if (trigger.relation() == ObjectRelation.THIS) {
            return;
        }
        if (trigger.relation().youControl() && !watcher.controllerId().equals(dyingControllerId)) {
            return;
        }
        // "Until end of turn, whenever a creature dies, its controller ...": the dying creature's controller rides
        // as the entry's non-targeting player, its toughness or power as the event value; "a nontoken creature
        // dies" binds the card that died (Consumed by History).
        CreatureDeathWatch watch = CreatureDeathWatch.of(trigger, Zone.BATTLEFIELD);
        switch (watch) {
            case ALLY -> enqueueRegisteredCreatureDeathTrigger(gameData, watcher, null, 0);
            case ANY -> enqueueRegisteredCreatureDeathTrigger(gameData, watcher, dyingControllerId,
                    Math.max(0, dyingToughnessAtDeath));
            case ALLY_NONTOKEN -> {
                if (!dyingCard.isToken()) {
                    enqueueRegisteredCreatureDeathTrigger(gameData, watcher, dyingControllerId,
                            Math.max(0, dyingPowerAtDeath));
                }
            }
            case ANY_NONTOKEN -> {
                if (!dyingCard.isToken()) {
                    enqueueRegisteredCreatureDeathTrigger(gameData, watcher, dyingCard.getId(), 0);
                }
            }
            default -> {
            }
        }
    }

    private void enqueueRegisteredCreatureDeathTrigger(GameData gameData, TemporaryGlobalTriggeredAbility watcher,
                                                       UUID targetId, int eventValue) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.sourceCard(),
                watcher.controllerId(),
                watcher.sourceCard().getName() + "'s ability",
                new ArrayList<>(List.of(watcher.effect())));
        entry.setTargetId(targetId);
        entry.setEventValue(eventValue);
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        log.info("Game {} - {} temporary global creature-death trigger fires",
                gameData.id, watcher.sourceCard().getName());
    }

    /** The "whenever a creature dies" trigger condition of {@code ability}, or {@code null} when it has none. */
    private static ZoneChangeTrigger creatureDeathTrigger(TriggeredAbility ability) {
        for (TriggerCondition condition : ability.conditions()) {
            if (condition.trigger() instanceof ZoneChangeTrigger trigger
                    && trigger.from().equals(Set.of(Zone.BATTLEFIELD)) && trigger.to() == Zone.GRAVEYARD
                    && trigger.causes().isEmpty()) {
                return trigger;
            }
        }
        return null;
    }

    private CardEffect snapshotDyingPermanentManaValue(GameData gameData, UUID controllerId,
                                                       CardEffect effect, Permanent dyingPermanent) {
        if (effect instanceof MayEffect may && may.wrapped() instanceof ReturnCardFromGraveyardEffect) {
            CardEffect snapshotted = snapshotDyingPermanentManaValue(
                    gameData, controllerId, may.wrapped(), dyingPermanent);
            return snapshotted == may.wrapped() ? effect : new MayEffect(
                    snapshotted, may.prompt(), may.elseEffect(), may.choicePlayer(), may.choicePlayerId());
        }
        if (!(effect instanceof ReturnCardFromGraveyardEffect returnEffect)
                || returnEffect.dynamicMaxManaValue() == null
                || dyingPermanent == null) {
            return effect;
        }
        int maxManaValue = amountEvaluationService.evaluateAtDeath(
                gameData, returnEffect.dynamicMaxManaValue(), controllerId, dyingPermanent);
        CardPredicate manaValueFilter = new CardMaxManaValuePredicate(maxManaValue);
        CardPredicate filter = returnEffect.filter() == null
                ? manaValueFilter
                : new CardAllOfPredicate(List.of(returnEffect.filter(), manaValueFilter));
        return returnEffect.toBuilder()
                .filter(filter)
                .dynamicMaxManaValue(null)
                .build();
    }

    private void collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
            GameData gameData, Permanent targetPermanent, UUID creatureControllerId, StackEntry triggeringEntry) {
        if (!gameQueryService.isCreature(gameData, targetPermanent)) return;
        if (creatureControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            List<CardEffect> effects = new ArrayList<>(TargetWatch.ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.effectsOf(source.getCard()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source,
                    TargetWatch.ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.trigger()));
            if (effects.isEmpty()) continue;

            enqueueTargetTriggeredAbility(gameData, targetPermanent, creatureControllerId, source,
                    new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source.getCard(),
                            creatureControllerId,
                            source.getCard().getName() + "'s triggered ability",
                            effects,
                            null,
                            source.getId()
                    ));

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} ally-creature-becomes-target trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    private void collectAllyCreatureOrCreatureSpellBecomesTargetOfOpponentTriggers(
            GameData gameData, StackEntry targetedEntry, StackEntry triggeringEntry) {
        if (targetedEntry == null || targetedEntry.getEntryType() != StackEntryType.CREATURE_SPELL) return;

        UUID creatureControllerId = targetedEntry.getControllerId();
        if (creatureControllerId == null || creatureControllerId.equals(triggeringEntry.getControllerId())) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(creatureControllerId);
        if (battlefield == null) return;

        for (Permanent source : List.copyOf(battlefield)) {
            List<CardEffect> effects = new ArrayList<>(TargetWatch.ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.effectsOf(source.getCard()));
            effects.addAll(grantedTriggeredAbilitySupport.grantedTriggerEffects(
                    gameData, source,
                    TargetWatch.ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY.trigger()));
            if (effects.isEmpty()) continue;

            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    creatureControllerId,
                    source.getCard().getName() + "'s triggered ability",
                    effects,
                    null,
                    source.getId()
            ));

            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s triggered ability triggers."));
            log.info("Game {} - {} ally-creature-spell-becomes-target trigger queued",
                    gameData.id, source.getCard().getName());
        }
    }

    /**
     * Points the triggered abilities already on the stack that deal damage to the controller of the permanent an
     * Aura enchants at that controller as the permanent dies: they can no longer see the permanent.
     */
    public void retargetEnchantedPermanentControllerDamage(GameData gameData, UUID dyingPermanentId,
                                                           UUID dyingPermanentControllerId) {
        Map<UUID, Permanent> auras = new LinkedHashMap<>();
        gameData.forEachPermanent((playerId, perm) -> auras.put(perm.getId(), perm));
        gameData.simultaneousDyingPermanents.forEach(auras::putIfAbsent);
        for (Permanent perm : auras.values()) {
            if (!dyingPermanentId.equals(perm.getAttachedTo())
                    || perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                continue;
            }
            for (StackEntry pending : gameData.stack) {
                if (pending.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && perm.getId().equals(pending.getSourcePermanentId())
                        && pending.getEffectsToResolve().stream().anyMatch(
                        e -> e instanceof DealDamageToEnchantedPermanentControllerEffect)) {
                    pending.setTargetId(dyingPermanentControllerId);
                    if (pending.getSourcePermanentSnapshot() == null) {
                        pending.setSourcePermanentSnapshot(new Permanent(perm));
                    }
                }
            }
        }
    }

    /**
     * Emblems' "whenever an artifact is put into your graveyard from the battlefield" abilities. An emblem is no
     * object the trigger pipeline lists, so its abilities are still collected as the artifact leaves.
     */
    public void checkEmblemArtifactGraveyardTriggers(GameData gameData, UUID graveyardOwnerId,
                                                     UUID artifactControllerId, Card artifactCard,
                                                     int artifactManaValue, Map<CounterType, Integer> artifactCounters,
                                                     boolean wasSacrificed) {
        collectEmblemArtifactGraveyardTriggers(gameData, graveyardOwnerId, artifactCard,
                new TriggerContext.ArtifactGraveyard(graveyardOwnerId, artifactControllerId, artifactCard,
                        artifactManaValue, artifactCounters, wasSacrificed));
    }

    private void collectEmblemArtifactGraveyardTriggers(GameData gameData, UUID graveyardOwnerId,
                                                        Card artifactCard, TriggerContext.ArtifactGraveyard context) {
        if (artifactCard == null || artifactCard.isToken()) {
            return;
        }
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(graveyardOwnerId)) {
                continue;
            }
            for (CardEffect effect : emblem.staticEffects()) {
                if (!(effect instanceof EmblemArtifactGraveyardReturnTriggerEffect)) {
                    continue;
                }
                var match = new EmblemTriggerMatchContext(
                        gameData, emblem, emblem.controllerId(), artifactCard, effect);
                registry.dispatchEmblem(match, effect, context);
            }
        }
    }

    /** Fires "whenever you win a coin flip" triggers for the winning player's battlefield. */
    public void checkControllerWinsCoinFlipTriggers(GameData gameData, UUID winningPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(winningPlayerId);
        TriggerContext ctx = new TriggerContext.CoinFlipWon(winningPlayerId);
        if (battlefield != null) {
            for (Permanent perm : List.copyOf(battlefield)) {
                if (!hasActiveTriggeredAbilities(gameData, perm)) continue;
                for (CardEffect effect : ActionWatch.ON_CONTROLLER_WINS_COIN_FLIP.effectsOf(perm.getCard())) {
                    var match = new TriggerMatchContext(gameData, perm, winningPlayerId, effect);
                    registry.dispatch(match, ActionWatch.ON_CONTROLLER_WINS_COIN_FLIP, effect, ctx);
                }
            }
        }

        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!hasActiveTriggeredAbilities(gameData, permanent)) return;
            for (CardEffect effect : ActionWatch.ON_ANY_PLAYER_WINS_COIN_FLIP.effectsOf(permanent.getCard())) {
                var match = new TriggerMatchContext(gameData, permanent, controllerId, effect);
                registry.dispatch(match, ActionWatch.ON_ANY_PLAYER_WINS_COIN_FLIP, effect, ctx);
            }
        });
    }

    /**
     * Whether a permanent's printed and granted abilities are still active, so its triggered
     * abilities can trigger. Mirrors the checks {@link #dispatchWatch} applies before dispatching.
     */
    private boolean hasActiveTriggeredAbilities(GameData gameData, Permanent perm) {
        if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return false;
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
        return !staticBonus.losesAllAbilities() && !staticBonus.losesAllNonManaAbilities()
                && !gameQueryService.hasLostPrintedAbilities(gameData, perm);
    }

    /**
     * Queues one "copy that spell" delayed trigger per unit of instant/sorcery copy-rider mana
     * (Primal Wellspring, Pyromancer's Goggles) spent on this matching spell (CR 106.6a); the copy
     * may choose new targets.
     */
    private void queueManaLinkedSpellCopyTriggers(GameData gameData, Card spellCard, UUID castingPlayerId,
                                                  int copies) {
        if (copies <= 0) return;
        StackEntry spellEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getTargetableId().equals(spellCard.getId())) {
                spellEntry = se;
                break;
            }
        }
        if (spellEntry == null) return;
        for (int copyNumber = 0; copyNumber < copies; copyNumber++) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    spellCard,
                    castingPlayerId,
                    "Copy " + spellCard.getName(),
                    new ArrayList<>(List.of(new CopyControllerCastSpellEffect(
                            new StackEntry(spellEntry), castingPlayerId)))
            ));
        }
        gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
        log.info("Game {} - {} mana-linked spell-copy trigger(s) queued for {}",
                gameData.id, copies, spellCard.getName());
    }

    /**
     * Queues one copy trigger per unit of copy-rider mana (Sunken Palace) spent to activate this
     * non-mana ability (CR 106.6a).
     */
    public void checkManaLinkedAbilityCopyTrigger(GameData gameData, UUID activatingPlayerId,
                                                   StackEntry abilityEntry, ActivatedAbility ability) {
        ManaPool pool = gameData.playerManaPools.get(activatingPlayerId);
        int pendingCopies = pool == null ? 0 : pool.takeSpentCopyGrantingMana(ManaCopyRider.SPELL_OR_ABILITY);
        if (pool != null) {
            pool.discardSpentCopyGrantingMana();
        }
        if (abilityEntry == null) return;
        for (int i = 0; i < pendingCopies; i++) {
            CardEffect copyEffect = new CopyControllerActivatedAbilityEffect(
                    new StackEntry(abilityEntry), ability, activatingPlayerId);
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    abilityEntry.getCard(),
                    activatingPlayerId,
                    abilityEntry.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(copyEffect))
            ));
            gameLogService.append(gameData,
                    GameLog.textCardText("A copy of ", abilityEntry.getCard(), "'s ability is created."));
            log.info("Game {} - mana-linked ability copy trigger queued for {}", gameData.id,
                    abilityEntry.getCard().getName());
        }
    }

    /**
     * Copy-rider mana spent to activate a mana ability triggers, but mana abilities can't be
     * copied, so the spent rider does nothing.
     */
    public void consumeManaLinkedCopyOnManaAbility(GameData gameData, UUID playerId) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        if (pool != null) {
            pool.discardSpentCopyGrantingMana();
        }
    }

    private boolean queueGraveyardCreaturePutTrigger(GameData gameData, Card sourceCard, UUID controllerId,
                                                      CardEffect resolved) {
        long sourceEntryVersion = gameData.graveyardEntryVersion(sourceCard.getId());
        if (resolved instanceof ConditionalEffect conditional
                && conditional.condition() instanceof SourceCardInGraveyard) {
            resolved = new ConditionalEffect(new SourceCardInGraveyard(sourceEntryVersion),
                    conditional.wrapped(), conditional.interveningIf(), conditional.triggerTimeOnly());
        }
        if (resolved instanceof MayPayManaEffect mayPay) {
            gameData.queueMayAbility(sourceCard, controllerId, mayPay, null);
        } else if (resolved instanceof MayEffect may) {
            gameData.queueMayAbility(sourceCard, controllerId, may);
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    controllerId,
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(resolved))
            ));
        }
        // CR 400.7: the trigger refers to this stay of the card in the graveyard; once the card
        // leaves and returns, it is a new object the trigger can no longer move.
        gameData.stack.getLast().setTriggeringCardGraveyardEntryVersion(sourceEntryVersion);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} graveyard trigger fires when a creature enters a graveyard",
                gameData.id, sourceCard.getName());
        return true;
    }

    private void queueGraveyardCreatureDeathTrigger(GameData gameData, Card sourceCard, UUID controllerId,
                                                     CardEffect effect, Card dyingCard,
                                                     UUID dyingCreatureControllerId) {
        if (effect instanceof MayPayManaEffect mayPay) {
            gameData.queueMayAbility(sourceCard, controllerId, mayPay, null);
        } else if (effect instanceof MayEffect may) {
            gameData.queueMayAbility(sourceCard, controllerId, may);
        } else {
            StackEntry triggerEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    controllerId,
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect))
            );
            triggerEntry.setTriggeringCardId(dyingCard.getId());
            LoseLifeEffect loseLife = findLoseLifeEffect(effect);
            if (loseLife != null && loseLife.recipient() == LoseLifeRecipient.DYING_CREATURE_CONTROLLER) {
                triggerEntry.setTargetId(dyingCreatureControllerId);
            }
            gameData.stack.add(triggerEntry);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} graveyard creature-death trigger queued",
                gameData.id, sourceCard.getName());
    }

    private LoseLifeEffect findLoseLifeEffect(CardEffect effect) {
        if (effect instanceof LoseLifeEffect loseLife) {
            return loseLife;
        }
        if (effect instanceof ConditionalEffect conditional) {
            return findLoseLifeEffect(conditional.wrapped());
        }
        return null;
    }

    /** Fires "whenever you lose a coin flip" triggers for the losing player's battlefield. */
    public void checkControllerLosesCoinFlipTriggers(GameData gameData, UUID losingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(losingPlayerId);
        if (battlefield == null) return;

        TriggerContext ctx = new TriggerContext.CoinFlipLost(losingPlayerId);
        for (Permanent perm : List.copyOf(battlefield)) {
            for (CardEffect effect : ActionWatch.ON_CONTROLLER_LOSES_COIN_FLIP.effectsOf(perm.getCard())) {
                var match = new TriggerMatchContext(gameData, perm, losingPlayerId, effect);
                registry.dispatch(match, ActionWatch.ON_CONTROLLER_LOSES_COIN_FLIP, effect, ctx);
            }
        }
    }

    private void collectPlanarCreatureDeathTriggers(GameData gameData, Card dyingCard,
                                                    Permanent dyingPermanent,
                                                    UUID dyingCreatureControllerId) {
        if (gameData.planechase == null || gameData.planechase.controllerId == null) return;

        UUID planarControllerId = gameData.planechase.controllerId;
        for (var object : List.copyOf(gameData.planechase.faceUp)) {
            for (CardEffect effect : object.getCard().getTriggerEffects(Triggers.creatureDies(ObjectRelation.ANY))) {
                CardEffect resolved = unwrapCreatureDeathConditional(
                        effect, dyingCard, dyingPermanent, gameData, planarControllerId);
                if (resolved == null) continue;
                if (resolved instanceof DyingCreatureCardAwareEffect aware) {
                    resolved = bindDyingCard(gameData, aware, dyingCard);
                }
                if (resolved.targetSpec().targetPredicate() != null) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            object.getCard(), planarControllerId, List.of(resolved), false,
                            object.getCard().getTargetFilter(), 0, null, null, false, null, null,
                            planarControllerId, object.copy()));
                    continue;
                }

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        object.getCard(),
                        planarControllerId,
                        object.getCard().getName() + "'s ability",
                        List.of(resolved));
                entry.setSourcePlanarObject(object.copy());
                entry.setTriggeringCardId(dyingCard.getId());
                entry.setTriggeringPermanentId(dyingPermanent.getId());
                entry.setTriggeringPermanentControllerId(dyingCreatureControllerId);
                entry.setTriggeringPermanentPowerAtTrigger(Math.max(0, dyingPermanent.getEffectivePower()));
                entry.setEventValue(Math.max(0, dyingPermanent.getEffectivePower()));
                gameData.enqueueTrigger(entry);
            }
        }
    }
    /**
     * Face-up planes' "whenever a nonblack card is put into a graveyard from anywhere" abilities. A plane is no
     * object the trigger pipeline lists, so its abilities are still collected as the card arrives.
     */
    public void checkPlanarNonblackCardPutIntoGraveyardTriggers(GameData gameData, UUID graveyardOwnerId, Card card) {
        if (card.getColors() != null && card.getColors().contains(CardColor.BLACK) || gameData.planechase == null) {
            return;
        }
        var ctx = new TriggerContext.CardPutIntoGraveyard(card, graveyardOwnerId);
        var trigger = Triggers.putIntoGraveyardFromAnywhere(Triggers.NONBLACK_CARD, ObjectRelation.ANOTHER);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (CardEffect effect : object.getCard().getTriggerEffects(trigger)) {
                registry.dispatch(new TriggerMatchContext(gameData, null, gameData.planechase.controllerId,
                                effect, object.getCard(), object.copy()),
                        ZoneChangeWatch.ON_NONBLACK_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, effect, ctx);
            }
        }
    }


    /** Fires effects that care when the given player foretells a card. */
    public void checkControllerForetellTriggers(GameData gameData, UUID foretellingPlayerId,
                                                Card foretoldCard) {
        if (foretellingPlayerId == null || foretoldCard == null) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(foretellingPlayerId);
        if (battlefield == null) return;

        var ctx = new TriggerContext.Foretell(foretellingPlayerId, foretoldCard);
        for (Permanent perm : List.copyOf(battlefield)) {
            dispatchWatch(gameData, perm, foretellingPlayerId, ActionWatch.ON_CONTROLLER_FORETELLS, ctx);
        }
    }

    private void collectCreatureDeathTriggerWatchers(GameData gameData, Permanent dyingPermanent) {
        for (CreatureDeathTriggerWatcher watcher : List.copyOf(gameData.creatureDeathTriggerWatchers)) {
            if (watcher.sourcePermanentId() != null && watcher.sourcePermanentId().equals(dyingPermanent.getId())) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    watcher.sourcePermanentId());
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers because a creature died",
                    gameData.id, watcher.sourceCard().getName());
        }
        for (TargetedCreatureDeathTriggerWatcher watcher
                : List.copyOf(gameData.targetedCreatureDeathTriggerWatchers)) {
            if (!watcher.watchedPermanentId().equals(dyingPermanent.getId())) {
                continue;
            }
            gameData.targetedCreatureDeathTriggerWatchers.remove(watcher);
            if (dyingPermanent.getCounterCount(watcher.counterType()) <= 0) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    (UUID) null);
            entry.setTriggeringPermanentId(dyingPermanent.getId());
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers because its watched creature died",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /** Fires delayed triggers registered for creatures damaged by a particular source object. */
    public void triggerDelayedDamagedCreatureDeathTriggers(GameData gameData, Card dyingCard,
                                                            UUID dyingCreatureControllerId) {
        if (dyingCard == null) {
            return;
        }
        UUID dyingCardId = dyingCard.getId();
        for (DamagedCreatureDeathTriggerWatcher watcher
                : List.copyOf(gameData.damagedCreatureDeathTriggerWatchers)) {
            if (!watcher.allControllers() && !watcher.controllerId().equals(dyingCreatureControllerId)
                    || !gameData.creatureCardsDamagedThisTurnBySource
                    .getOrDefault(watcher.damageSourceId(), Set.of()).contains(dyingCardId)) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    (UUID) null);
            entry.setTriggeringCardId(dyingCardId);
            // "its controller loses N life" reads the dying creature's last-known controller from the
            // entry's targetId; the ability itself chooses no target.
            if (watcher.effect() instanceof LoseLifeEffect lose
                    && lose.recipient() == LoseLifeRecipient.DYING_CREATURE_CONTROLLER) {
                entry.setTargetId(dyingCreatureControllerId);
            }
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers because a creature it damaged died",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /**
     * Emblem creature-death triggers — Liliana, Defiant Necromancer's "Whenever a creature dies,
     * return it to the battlefield under your control at the beginning of the next end step."
     * Emblems live outside the battlefield, so the permanent sweep above never sees them.
     */
    private void collectEmblemCreatureDeathTriggers(GameData gameData, Card dyingCard,
                                                     TriggerContext.CreatureDeath context) {
        for (Emblem emblem : gameData.emblems) {
            for (CardEffect effect : emblem.staticEffects()) {
                int previousCopies = gameData.beginTriggeredAbilityCopies(1 +
                        gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggersForEmblem(
                                gameData, emblem.controllerId()));
                try {
                    if (effect instanceof EmblemCreatureDeathTriggerEffect) {
                        var match = new EmblemTriggerMatchContext(
                                gameData, emblem, emblem.controllerId(), dyingCard, effect);
                        registry.dispatchEmblem(match, effect, context);
                        continue;
                    }
                    if (!(effect instanceof RegisterDelayedReturnDyingCreatureUnderControlEffect delayedReturn)) {
                        continue;
                    }
                    Card source = emblem.sourceCard();
                    String desc = (source != null ? source.getName() : "Emblem") + "'s emblem";
                    StackEntry entry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            source != null ? source : dyingCard,
                            emblem.controllerId(),
                            desc,
                            new ArrayList<>(List.of(delayedReturn))
                    );
                    entry.setTriggeringCardId(dyingCard.getId());
                    gameData.stack.add(entry);
                    gameLogService.append(gameData, GameLog.text(desc + " triggers (a creature died)."));
                    log.info("Game {} - {} schedules a delayed return of {}", gameData.id, desc, dyingCard.getName());
                } finally {
                    gameData.restoreTriggeredAbilityCopies(previousCopies);
                }
            }
        }
    }

    private boolean dispatchCounterReturnForLeavingSource(GameData gameData, UUID controllerId,
                                                          Permanent source, Object slot, List<CardEffect> effects,
                                                          TriggerContext context) {
        boolean triggered = false;
        for (CardEffect effect : effects) {
            if (effect instanceof com.github.laxika.magicalvibes.model.effect.ReturnTriggeringPermanentToBattlefieldWithCounterEffect
                    || effect instanceof com.github.laxika.magicalvibes.model.effect.DyingCreatureControllerMayDrawCardEffect) {
                triggered |= dispatch(new TriggerMatchContext(gameData, source, controllerId, effect), slot, effect,
                        context);
            }
        }
        return triggered;
    }

    /**
     * "Whenever a permanent is put into a graveyard from the battlefield": each effect through its handler. The
     * collector leaves the watcher's own move out, unless the ability watches "a permanent you control".
     */
    private boolean dispatchAnyPermanentDeathTriggersForWatcher(GameData gameData, UUID watcherControllerId,
                                                                Permanent watcher, List<CardEffect> effects,
                                                                boolean ownMoveCounts, Card dyingCard,
                                                                TriggerContext.AnyPermanentGraveyard ctx) {
        if (!ownMoveCounts && watcher.getCard().getId().equals(dyingCard.getId())) {
            return false;
        }
        boolean anyTriggered = false;
        for (CardEffect effect : effects) {
            var match = new TriggerMatchContext(gameData, watcher, watcherControllerId, effect);
            anyTriggered |= dispatch(match,
                    ZoneChangeWatch.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD, effect, ctx);
        }
        return anyTriggered;
    }

    /** Whether {@code candidate}'s ability watches "a permanent you control", which the source itself is too. */
    private static boolean watchesPermanentsYouControl(TriggerCandidate candidate) {
        return candidate.ability().conditions().stream()
                .anyMatch(condition -> condition.trigger() instanceof ZoneChangeTrigger move
                        && move.relation() == ObjectRelation.YOU_CONTROL);
    }

    /**
     * Fires the delayed "when that permanent leaves the battlefield" triggers that watch {@code target}
     * (CR 603.7): the destination-keyed {@code DelayedZoneChangeTrigger}s and the leaves-the-battlefield
     * abilities a resolving spell registered for it. The permanent's own leaves-the-battlefield abilities go
     * through the trigger pipeline.
     */
    public void checkDelayedLeavesBattlefieldTriggers(GameData gameData, Permanent target, Zone destination) {
        for (var delayed : gameData.drainDelayedActions(
                com.github.laxika.magicalvibes.model.action.DelayedZoneChangeTrigger.class,
                action -> action.watchedPermanentId().equals(target.getId()))) {
            CardEffect effect = delayed.effectsByDestination().get(destination);
            if (effect == null) continue;
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, delayed.sourceCard(),
                    delayed.controllerId(), delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(effect)), null, delayed.sourcePermanentId());
            entry.setTriggeringCardId(target.getOriginalCard().getId());
            if (destination == Zone.EXILE) {
                entry.setTriggeringCardExileEntryVersion(
                        gameData.findExiledCard(entry.getTriggeringCardId()) == null ? -1L
                                : gameData.exileEntryVersions.getOrDefault(entry.getTriggeringCardId(), 0L));
            }
            entry.setTriggeringCardGraveyardEntryVersion(
                    destination == Zone.GRAVEYARD ? -1L : 0L);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
        }
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().conditions().getFirst().trigger()
                    .equals(Triggers.leavesBattlefield(ObjectRelation.THIS))
                    || !target.getId().equals(watcher.watchedPermanentId())) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(watcher.effect())),
                    null,
                    target.getId());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /**
     * Fires delayed "when that creature leaves the battlefield this turn, sacrifice this creature"
     * triggers (Kjeldoran Elite Guard). Drains matching {@link DelayedSacrificeSourceWhenTargetLeaves}
     * entries and enqueues a {@link SacrificeSelfEffect} for each. Called from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedSacrificeSourceWhenTargetLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedSacrificeSourceWhenTargetLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedSacrificeSourceWhenTargetLeaves delayed : gameData.drainDelayedActions(
                DelayedSacrificeSourceWhenTargetLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            StackEntry se = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new SacrificeSelfEffect())),
                    null,
                    delayed.sourcePermanentId());
            se.setNonTargeting(true);
            gameData.enqueueTrigger(se);
            gameLogService.append(gameData,
                    GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
            log.info("Game {} - {} delayed leave-trigger fires (watched {} left); sacrifice source {}",
                    gameData.id, delayed.sourceCard().getName(), leavingPermanent.getCard().getName(),
                    delayed.sourcePermanentId());
        }
    }

    /**
     * Fires delayed "when this creature leaves the battlefield this turn, sacrifice that creature"
     * triggers (Phantasmal Mount). Drains matching {@link DelayedSacrificeTargetWhenSourceLeaves}
     * entries and enqueues a {@link SacrificeSelfEffect} against the pumped target for each. Called
     * from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedSacrificeTargetWhenSourceLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedSacrificeTargetWhenSourceLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedSacrificeTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedSacrificeTargetWhenSourceLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            StackEntry se = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new SacrificeSelfEffect())),
                    null,
                    delayed.targetPermanentId());
            se.setNonTargeting(true);
            gameData.enqueueTrigger(se);
            gameLogService.append(gameData,
                    GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
            log.info("Game {} - {} delayed leave-trigger fires (source {} left); sacrifice target {}",
                    gameData.id, delayed.sourceCard().getName(), leavingPermanent.getCard().getName(),
                    delayed.targetPermanentId());
        }
    }

    /**
     * Fires delayed "when this artifact leaves the battlefield this turn, destroy that creature"
     * triggers (War Barge). Drains matching registrations and enqueues a non-targeting destruction
     * for each captured target. Called from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedDestroyTargetWhenSourceLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedDestroyTargetWhenSourceLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedDestroyTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedDestroyTargetWhenSourceLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            enqueueDelayedDestroyTarget(gameData, delayed, leavingPermanent);
        }
    }

    /**
     * Fires delayed "when this creature ... becomes untapped, destroy that creature" triggers (Merieke Ri
     * Berit) for the permanent that just became untapped. Each fires only once (CR 603.7b).
     */
    private void processDelayedDestroyTargetWhenSourceUntaps(GameData gameData, Permanent untappedPermanent) {
        if (!gameData.hasDelayedAction(DelayedDestroyTargetWhenSourceLeaves.class)) {
            return;
        }
        UUID untappedId = untappedPermanent.getId();
        for (DelayedDestroyTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedDestroyTargetWhenSourceLeaves.class,
                d -> d.alsoWhenUntapped() && untappedId.equals(d.watchedPermanentId()))) {
            enqueueDelayedDestroyTarget(gameData, delayed, untappedPermanent);
        }
    }

    /**
     * Fires delayed "when you lose control of this creature, exile that creature" triggers (Coffin Queen)
     * for a permanent whose control just changed away from {@code previousControllerId}. Each fires only
     * once (CR 603.7b).
     */
    public void processDelayedTargetWhenSourceLosesControl(GameData gameData, Permanent permanent,
                                                           UUID previousControllerId) {
        if (!gameData.hasDelayedAction(DelayedDestroyTargetWhenSourceLeaves.class)) {
            return;
        }
        for (DelayedDestroyTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedDestroyTargetWhenSourceLeaves.class,
                d -> d.alsoWhenControlLost() && permanent.getId().equals(d.watchedPermanentId())
                        && previousControllerId.equals(d.controllerId()))) {
            enqueueDelayedDestroyTarget(gameData, delayed, permanent);
        }
    }

    private void enqueueDelayedDestroyTarget(GameData gameData, DelayedDestroyTargetWhenSourceLeaves delayed,
                                             Permanent watchedPermanent) {
        StackEntry se = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                delayed.sourceCard(),
                delayed.controllerId(),
                delayed.sourceCard().getName() + "'s delayed trigger",
                new ArrayList<>(List.of(delayed.exile()
                        ? new com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect(
                                com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect.Mode.EXILE,
                                delayed.targetPermanentId())
                        : new DestroyLinkedPermanentEffect(true, delayed.targetPermanentId()))),
                null,
                List.of());
        se.setNonTargeting(true);
        gameData.enqueueTrigger(se);
        gameLogService.append(gameData,
                GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
        log.info("Game {} - {} delayed trigger fires (source {}); destroy target {}",
                gameData.id, delayed.sourceCard().getName(), watchedPermanent.getCard().getName(),
                delayed.targetPermanentId());
    }

    /**
     * Fires delayed counter triggers for cards that return from exile when their source leaves the
     * battlefield. The return controller is compared with the controller captured when the
     * source's enter-the-battlefield ability resolved.
     */
    public void processDelayedExileReturnCounterTriggers(GameData gameData, UUID leavingPermanentId,
                                                          List<PendingExileReturn> pendingReturns,
                                                          List<UUID> returnedPermanentIds) {
        List<DelayedExileReturnCounterTrigger> delayedTriggers = gameData.drainDelayedActions(
                DelayedExileReturnCounterTrigger.class,
                trigger -> leavingPermanentId.equals(trigger.watchedPermanentId()));
        if (delayedTriggers.isEmpty()) {
            return;
        }

        for (int i = 0; i < pendingReturns.size(); i++) {
            PendingExileReturn pending = pendingReturns.get(i);
            UUID returnedPermanentId = returnedPermanentIds.get(i);
            if (returnedPermanentId == null) {
                continue;
            }
            for (DelayedExileReturnCounterTrigger delayed : delayedTriggers) {
                if (!pending.controllerId().equals(delayed.controllerId())) {
                    continue;
                }
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        delayed.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new PutCounterOnTargetPermanentEffect(
                                delayed.counterType(), delayed.counterAmount()))),
                        returnedPermanentId,
                        (UUID) null);
                trigger.setNonTargeting(true);
                gameData.enqueueTrigger(trigger);
                gameLogService.append(gameData,
                        GameLog.cardThen(delayed.sourceCard(), "'s delayed ability triggers."));
                log.info("Game {} - {} delayed return counter trigger queued for {}",
                        gameData.id, delayed.sourceCard().getName(), pending.card().getName());
            }
        }
    }
    /**
     * Face-up planes' "whenever a creature leaves the battlefield" abilities (Takenuma). A plane is no object the
     * trigger pipeline lists, so its abilities are still collected as the creature leaves.
     */
    public void checkPlanarCreatureLeavesBattlefieldTriggers(GameData gameData, Permanent leavingPermanent,
                                                             boolean wasCreature, UUID leavingControllerId) {
        if (!wasCreature) return;
        UUID leavingId = leavingPermanent.getId();
        if (gameData.planechase == null || leavingControllerId == null) {
            return;
        }
        UUID planarControllerId = gameData.planechase.controllerId;
        if (planarControllerId == null) {
            return;
        }
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (CardEffect effect : object.getCard().getTriggerEffects(
                    Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER))) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        object.getCard(),
                        planarControllerId,
                        object.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)));
                entry.setSourcePlanarObject(object.copy());
                entry.setTriggeringPermanentId(leavingId);
                entry.setTriggeringPermanentControllerId(leavingControllerId);
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.text(object.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} planar trigger on another creature leaving the battlefield ({})",
                        gameData.id, object.getCard().getName(), leavingPermanent.getCard().getName());
            }
        }
    }

    /**
     * "Whenever another permanent leaves the battlefield" triggers. Called from every
     * leave-the-battlefield path after the permanent has been removed. Permanents that leave in the
     * same simultaneous event as the leaving permanent still see it (CR 603.10a), so their
     * snapshots are checked as well; an ability that was lost before the event does not trigger.
     */
    /**
     * Face-up schemes' "whenever another permanent leaves the battlefield" abilities, which trigger from the command
     * zone (CR 314.4). A scheme is no object the trigger pipeline lists, so its abilities are still collected as the
     * permanent leaves.
     */
    public void checkSchemePermanentLeavesBattlefieldTriggers(GameData gameData, Permanent leavingPermanent) {
        UUID leavingId = leavingPermanent.getId();

        UUID leavingOwnerId = leavingPermanent.getCard().getOwnerId();
        boolean leavingHasStaticEffects = !leavingPermanent.getCard().getStaticEffects().isEmpty();
        BiConsumer<UUID, Permanent> collect = (ownerId, perm) -> {
            if (perm.getId().equals(leavingId)) return;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()
                    || gameQueryService.hasLostAllAbilities(gameData, perm)) return;
            // CR 603.10a: look back to just before the event. An ability removed only by the leaving
            // permanent's own static effect (Humility) was not there to trigger.
            if (leavingHasStaticEffects
                    && gameQueryService.findPermanentById(gameData, perm.getId()) != null
                    && gameQueryService.withEnteringPermanent(gameData, leavingOwnerId != null ? leavingOwnerId : ownerId, leavingPermanent,
                    () -> gameQueryService.hasLostAllAbilities(gameData, perm))) return;
            for (CardEffect effect : schemeLeavesBattlefieldEffects(gameData, perm, ownerId, leavingPermanent)) {
                if (effect instanceof TransformSelfWhenChosenPermanentLeavesEffect
                        && !leavingId.equals(perm.getChosenPermanentId())) continue;
                CardEffect resolved = effect;
                if (resolved instanceof LeavingPermanentIdAwareEffect aware) {
                    resolved = aware.boundToLeavingPermanentId(leavingId);
                }
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        ownerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(resolved)),
                        null,
                        perm.getId()
                );
                if (resolved instanceof TransformSelfWhenChosenPermanentLeavesEffect) {
                    entry.setSourcePermanentSnapshot(new Permanent(perm));
                }
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.text(perm.getCard().getName() + "'s ability triggers."));
                log.info("Game {} - {} triggers on another permanent leaving the battlefield ({})",
                        gameData.id, perm.getCard().getName(), leavingPermanent.getCard().getName());
            }
        };
        // CR 314.4: a face-up scheme's triggered abilities trigger from the command zone.
        gameData.forEachFaceUpSchemeSource(collect);
    }

    /**
     * The effects of a face-up scheme's "whenever another permanent leaves the battlefield" abilities whose filter
     * {@code leaving} matches ("whenever your commander leaves the battlefield"), with the scheme as the filter's
     * source.
     */
    private List<CardEffect> schemeLeavesBattlefieldEffects(GameData gameData, Permanent scheme, UUID ownerId,
                                                            Permanent leaving) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(scheme.getOriginalCard().getId())
                .withSourceControllerId(ownerId)
                .withSourcePermanentId(scheme.getId())
                .withSourcePermanentSnapshot(scheme);
        List<CardEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : ZoneChangeWatch.ON_ANOTHER_PERMANENT_LEAVES_BATTLEFIELD
                .abilitiesOf(scheme.getCard())) {
            boolean matches = ability.conditions().stream()
                    .map(TriggerCondition::trigger)
                    .anyMatch(trigger -> trigger instanceof ZoneChangeTrigger move && (move.filter() == null
                            || predicateEvaluationService.matchesPermanentPredicate(leaving, move.filter(),
                            filterContext)));
            if (matches) {
                ability.effects().forEach(registration -> effects.add(registration.effect()));
            }
        }
        return effects;
    }

    private void enqueueAllyPermanentsLeaveBattlefieldTriggers(GameData gameData, Permanent source,
                                                               UUID controllerId, List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                        source.getCard(), controllerId, new ArrayList<>(List.of(effect))));
            } else {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                        source.getCard().getName() + "'s ability", new ArrayList<>(List.of(effect)),
                        null, source.getId()));
            }
            gameLogService.append(gameData, GameLog.text(source.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers when one or more permanents they control leave the battlefield",
                    gameData.id, source.getCard().getName());
        }
    }

    private void enqueueCreatureLeavesBattlefieldWithoutDyingTriggers(
            GameData gameData, Permanent source, UUID controllerId, List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                        source.getCard(), controllerId, new ArrayList<>(List.of(effect))));
            } else {
                gameData.enqueueTrigger(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, source.getCard(), controllerId,
                        source.getCard().getName() + "'s ability", new ArrayList<>(List.of(effect)),
                        null, source.getId()));
            }
            gameLogService.append(gameData, GameLog.text(source.getCard().getName() + "'s ability triggers."));
            log.info("Game {} - {} triggers on a creature leaving without dying",
                    gameData.id, source.getCard().getName());
        }
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, List<ChooseOneEffect.ChooseOneOption> chosen) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen);
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, List<ChooseOneEffect.ChooseOneOption> chosen, UUID triggeringCardId) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen, triggeringCardId);
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, List<ChooseOneEffect.ChooseOneOption> chosen, UUID triggeringCardId,
            UUID attackedTargetId) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen, triggeringCardId, attackedTargetId);
    }

    public void queueChosenTriggeredModalTrigger(GameData gameData, Card sourceCard, UUID controllerId,
            UUID sourcePermanentId, List<ChooseOneEffect.ChooseOneOption> chosen, UUID triggeringCardId,
            UUID attackedTargetId, UUID triggeringPermanentId) {
        triggeredAbilityQueueService.queueChosenTriggeredModalTrigger(gameData, sourceCard, controllerId,
                sourcePermanentId, chosen, triggeringCardId, attackedTargetId, triggeringPermanentId);
    }

    public void triggerDelayedPoisonOnDeath(GameData gameData, UUID dyingCreatureCardId, UUID controllerId) {
        Integer poisonAmount = gameData.creatureGivingControllerPoisonOnDeathThisTurn.remove(dyingCreatureCardId);
        if (poisonAmount == null || poisonAmount <= 0) {
            return;
        }

        poisonAmount = gameQueryService.applyPoisonCounterReplacement(gameData, controllerId, poisonAmount);
        if (poisonAmount <= 0) return;

        poisonAmount = gameQueryService.replacePoisonCounters(gameData, controllerId, poisonAmount);
        if (poisonAmount <= 0) return;
        int currentPoison = gameData.playerPoisonCounters.getOrDefault(controllerId, 0);
        gameData.playerPoisonCounters.put(controllerId, currentPoison + poisonAmount);
        checkYouPutCountersTriggers(gameData, controllerId, poisonAmount);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " gets " + poisonAmount + " poison counter"
                + (poisonAmount > 1 ? "s" : "") + " (delayed trigger: creature died this turn).";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} gets {} poison counter(s) (delayed trigger: creature died this turn)",
                gameData.id, playerName, poisonAmount);
    }

    /**
     * Delayed "return that card when it dies this turn" (Graceful Reprieve, Supernatural Stamina,
     * Adarkar Valkyrie): if the dying creature's card was registered, push a triggered ability that
     * returns it from its owner's graveyard to the battlefield. Fires at most once per registration.
     */
    public void triggerDelayedReturnOnDeath(GameData gameData, UUID dyingPermanentCardId, Card graveyardCard, UUID ownerId) {
        List<DelayedReturnOnDeath> delayedReturns = gameData.creaturesReturnedToBattlefieldOnDeathThisTurn
                .remove(dyingPermanentCardId);
        if (delayedReturns == null || delayedReturns.isEmpty()) {
            return;
        }

        for (DelayedReturnOnDeath delayedReturn : delayedReturns) {
            if (delayedReturn.requireControllerGraveyard()
                    && !delayedReturn.controllerId().equals(ownerId)) {
                continue;
            }
            UUID triggerControllerId = delayedReturn.controllerId();
            Card triggerSource = delayedReturn.sourceCard() != null ? delayedReturn.sourceCard() : graveyardCard;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    triggerSource,
                    triggerControllerId,
                    "Return " + graveyardCard.getName() + " to the battlefield",
                    new ArrayList<>(List.of(new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(
                            delayedReturn.enterTapped(), delayedReturn.returnUnderController())))
            );
            entry.setTriggeringCardId(graveyardCard.getId());
            entry.setTriggeringCardGraveyardEntryVersion(
                    gameData.graveyardEntryVersion(graveyardCard.getId()));
            gameData.stack.add(entry);

            gameLogService.append(gameData, GameLog.cardThen(graveyardCard, " will return to the battlefield (it died this turn)."));
            log.info("Game {} - Delayed return trigger: {} will return to the battlefield", gameData.id, graveyardCard.getName());
        }
    }

    /**
     * Resolves delayed effects registered for a permanent dying this turn. Creature-only registrations
     * and registrations that can also apply to other permanent types are kept separately, then combined
     * when the permanent dies.
     */
    public void triggerDelayedEffectOnDeath(GameData gameData, UUID dyingPermanentCardId,
                                            UUID dyingPermanentControllerId, int dyingPermanentPower,
                                            int dyingPermanentManaValue,
                                            Map<CounterType, Integer> dyingPermanentCounters) {
        triggerDelayedEffectOnDeath(gameData, dyingPermanentCardId, dyingPermanentControllerId,
                dyingPermanentPower, dyingPermanentManaValue, dyingPermanentCounters, null);
    }

    public void triggerDelayedEffectOnDeath(GameData gameData, UUID dyingPermanentCardId,
                                            UUID dyingPermanentControllerId, int dyingPermanentPower,
                                            int dyingPermanentManaValue,
                                            Map<CounterType, Integer> dyingPermanentCounters,
                                            UUID dyingPermanentId) {
        List<DelayedEffectOnDeath> registrations = new ArrayList<>();
        List<DelayedEffectOnDeath> creatureRegistrations =
                gameData.creatureTriggeringEffectOnDeathThisTurn.remove(dyingPermanentCardId);
        if (creatureRegistrations != null) {
            registrations.addAll(creatureRegistrations);
        }
        List<DelayedEffectOnDeath> permanentRegistrations =
                gameData.permanentTriggeringEffectOnDeathThisTurn.remove(dyingPermanentCardId);
        if (permanentRegistrations != null) {
            registrations.addAll(permanentRegistrations);
        }
        List<DelayedEffectOnDeath> graveyardRegistrations =
                gameData.permanentTriggeringEffectOnGraveyardEntry.remove(dyingPermanentCardId);
        if (graveyardRegistrations != null) {
            registrations.addAll(graveyardRegistrations);
        }
        if (registrations.isEmpty()) {
            return;
        }

        for (DelayedEffectOnDeath registration : registrations) {
            if (registration.targetPermanentId() != null
                    && !registration.targetPermanentId().equals(dyingPermanentId)) continue;

            CardEffect effect = registration.effect();
            if (effect instanceof DyingCreatureCountersAwareEffect countersAware) {
                effect = countersAware.boundToDyingCreatureCounters(dyingPermanentCounters);
            }
            if (effect instanceof ConditionalEffect conditional && conditional.interveningIf()
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forCard(registration.sourceCard(), registration.controllerId()))) {
                continue;
            }
            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                        registration.sourceCard(), registration.controllerId(), List.of(effect),
                        null, null, false));
                log.info("Game {} - Delayed death trigger: {} awaits a target",
                        gameData.id, registration.sourceCard().getName());
                continue;
            }

            StackEntry delayedEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    registration.sourceCard(),
                    registration.controllerId(),
                    registration.sourceCard().getName() + " triggers (a creature dealt damage this way died)",
                    new ArrayList<>(List.of(effect)),
                    dyingPermanentControllerId,
                    registration.sourcePermanentId());
            delayedEntry.setEventValue(Math.max(0, dyingPermanentPower));
            delayedEntry.setDyingPermanentManaValue(Math.max(0, dyingPermanentManaValue));
            delayedEntry.setTriggeringCardId(dyingPermanentCardId);
            delayedEntry.setTriggeringCardGraveyardEntryVersion(
                    gameData.graveyardEntryVersion(dyingPermanentCardId));
            gameData.stack.add(delayedEntry);

            log.info("Game {} - Delayed death trigger: {} triggers (a creature it damaged died this turn)",
                    gameData.id, registration.sourceCard().getName());
        }
    }

    // ── Enter-the-battlefield triggers ─────────────────────────────────

    public void checkDayNightChangeTriggers(GameData gameData, DayNight previous, DayNight current) {
        if (previous == current || previous == DayNight.NEITHER || current == DayNight.NEITHER) {
            return;
        }

        TriggerContext context = new TriggerContext.DayNightChange(previous, current);
        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent permanent : new ArrayList<>(battlefield)) {
                    dispatchWatch(gameData, permanent, controllerId,
                            MiscWatch.ON_DAY_NIGHT_CHANGE, context);
                }
            }
            checkGraveyardDayNightChangeTriggers(gameData, controllerId);
        }
        triggeredAbilityQueueService.processNextDayNightTriggerTarget(gameData);
    }

    private void checkGraveyardDayNightChangeTriggers(GameData gameData, UUID controllerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            for (CardEffect effect : gameQueryService.getEffectiveGraveyardTriggerEffects(
                    gameData, card, MiscWatch.GRAVEYARD_ON_DAY_NIGHT_CHANGE.trigger())) {
                StackEntry triggerEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s ability",
                        new ArrayList<>(List.of(effect))
                );
                // CR 400.7: the ability refers to this graveyard object, not a later one.
                triggerEntry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(card.getId()));
                gameData.enqueueTrigger(triggerEntry);
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
                log.info("Game {} - {} triggers from the graveyard when day/night changes",
                        gameData.id, card.getName());
            }
        }
    }

    public void processNextDayNightTriggerTarget(GameData gameData) {
        triggeredAbilityQueueService.processNextDayNightTriggerTarget(gameData);
    }

    /**
     * What a creature entering under {@code controllerId}'s control triggers besides the card abilities the trigger
     * pipeline matches: evolve (CR 702.100d), the watchers and boons a resolved spell left, the "whenever a creature
     * you control enters" abilities a spell registered until end of turn, and emblems.
     */
    public void checkAllyCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCreature, int extraWizardTriggers) {
        if (!enteringPermanentHasType(gameData, enteringCreature, CardType.CREATURE)) return;

        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn() || perm.isFaceDown() || perm.getCard() == enteringCreature) {
                continue;
            }
            if (enteringPermanent != null && gameQueryService.hasKeyword(gameData, perm, Keyword.EVOLVE)) {
                // CR 702.100d: each instance of evolve (printed or granted) triggers separately.
                int evolveInstances = gameQueryService.triggerKeywordInstances(gameData, perm, Keyword.EVOLVE);
                collectEvolveTrigger(gameData, controllerId, perm, enteringPermanent,
                        evolveInstances + extraWizardTriggers);
            }
        }

        for (CreatureEntersTriggerWatcher watcher
                : List.copyOf(gameData.allyCreatureEntersTriggerWatchers)) {
            if (!watcher.controllerId().equals(controllerId) || enteringPermanent == null) {
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setTargetId(enteringPermanent.getId());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }

        if (enteringPermanent != null && !gameData.deferCreatureEntersBoons) {
            collectAllyBoonTriggers(gameData, controllerId, List.of(enteringPermanent.getId()), true);
        }

        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watches(watcher, ZoneChangeWatch.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
                    || !watcher.controllerId().equals(controllerId)) {
                continue;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary ally creature-enters trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }

        if (enteringPermanent != null) {
            for (Emblem emblem : gameData.emblems) {
                if (!emblem.controllerId().equals(controllerId)) continue;
                for (CardEffect effect : emblem.staticEffects()) {
                    Card sourceCard = emblem.sourceCard() != null ? emblem.sourceCard() : enteringCreature;
                    if (effect instanceof MayFightTargetCreatureOnAllyCreatureEntersEffect) {
                        gameData.queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                                sourceCard, controllerId,
                                List.of(new MayEffect(
                                        new EnteringCreatureFightsTargetCreatureEffect(),
                                        "Have it fight target creature?")),
                                null,
                                enteringPermanent.getId()));
                        gameLogService.append(gameData, GameLog.text(
                                sourceCard.getName() + "'s emblem triggers for " + enteringCreature.getName()
                                        + " entering."));
                    } else if (effect instanceof DealDamageToAnyTargetOnAllyCreatureEntersEffect) {
                        gameData.queueInteraction(new PermanentChoiceContext.EnteringPermanentAnyTargetTrigger(
                                sourceCard, controllerId,
                                List.of(new DealDamageToAnyTargetEffect(new SourcePower())),
                                enteringPermanent.getId()));
                        gameLogService.append(gameData, GameLog.text(
                                sourceCard.getName() + "'s emblem triggers for " + enteringCreature.getName()
                                        + " entering."));
                    }
                }
            }
        }
    }

    /**
     * Whether a temporary global ability has a trigger condition {@code watch} stands for, or one that narrows it down
     * by a filter ("a nontoken creature an opponent controls"), which {@code entering} matches as it entered.
     */
    private boolean watchesEntering(GameData gameData, TemporaryGlobalTriggeredAbility watcher, ZoneChangeWatch watch,
                                    Permanent entering) {
        for (TriggerCondition condition : watcher.ability().conditions()) {
            if (!(condition.trigger() instanceof ZoneChangeTrigger trigger)
                    || ZoneChangeWatch.of(watcher.ability(), trigger, Zone.BATTLEFIELD) != watch) {
                continue;
            }
            if (trigger.filter() == null || predicateEvaluationService.matchesPermanentPredicate(entering,
                    trigger.filter(), FilterContext.of(gameData).withSourceControllerId(watcher.controllerId())
                            .withSourceCardId(watcher.sourceCard().getId()))) {
                return true;
            }
        }
        return false;
    }

    /** Whether a temporary global ability's trigger is one of those {@code watch} stands for. */
    private static boolean watches(TemporaryGlobalTriggeredAbility watcher, ZoneChangeWatch watch) {
        return watcher.ability().conditions().stream().anyMatch(condition -> watch.keys().stream()
                .anyMatch(key -> key.trigger().equals(condition.trigger())));
    }

    private void collectCreatureSpellBoonTriggers(GameData gameData, Card spellCard,
                                                 UUID castingPlayerId, boolean castFaceDown) {
        if (!castFaceDown && !spellCard.hasType(CardType.CREATURE)) return;
        for (Boon boon : List.copyOf(gameData.boons)) {
            if (boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.CREATURE_CAST
                    || !castingPlayerId.equals(boon.controllerId())) continue;
            gameData.boons.remove(boon);
            if (boon.remainingUses() > 1) {
                gameData.boons.add(new Boon(boon.controllerId(), boon.sourceCard(), boon.effect(),
                        boon.remainingUses() - 1, boon.trigger(), boon.targetFilter()));
            }
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, boon.sourceCard(),
                    boon.controllerId(), boon.sourceCard().getName() + "'s boon",
                    new ArrayList<>(List.of(boon.effect())));
            entry.setTriggeringCardId(spellCard.getId());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
        }
    }

    /**
     * Queues the controller's boons that fire when permanents enter. {@link BoonTrigger#CREATURE_ENTERS}
     * boons watch creatures only; {@link BoonTrigger#PERMANENT_ENTERS} boons watch every permanent, so the
     * creature entries ({@code creatureEntries}) and the noncreature entries are offered separately.
     */
    private void collectAllyBoonTriggers(GameData gameData, UUID controllerId,
                                         List<UUID> enteringPermanentIds, boolean creatureEntries) {
        if (enteringPermanentIds == null || enteringPermanentIds.isEmpty()) return;

        for (Boon boon : List.copyOf(gameData.boons)) {
            if ((boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.CREATURE_ENTERS
                    && boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.ONE_OR_MORE_CREATURES_ENTER
                    && boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.PERMANENT_ENTERS)
                    || !boon.controllerId().equals(controllerId)) continue;

            Permanent enteringPermanent = null;
            CardEffect resolved = null;
            List<UUID> matchingEnteringIds = new ArrayList<>();
            for (UUID enteringPermanentId : enteringPermanentIds) {
                Permanent candidate = gameQueryService.findPermanentById(gameData, enteringPermanentId);
                if (candidate == null) continue;
                boolean candidateIsCreature = gameQueryService.isCreature(gameData, candidate);
                if ((boon.trigger() == com.github.laxika.magicalvibes.model.BoonTrigger.CREATURE_ENTERS
                        || boon.trigger() == com.github.laxika.magicalvibes.model.BoonTrigger.ONE_OR_MORE_CREATURES_ENTER)
                        ? !candidateIsCreature
                        : candidateIsCreature != creatureEntries) {
                    continue;
                }
                CardEffect candidateEffect = unwrapTriggeringCardConditional(
                        boon.effect(), candidate.getCard(), gameData, controllerId, boon.sourceCard().getId());
                if (candidateEffect != null) {
                    if (enteringPermanent == null) {
                        enteringPermanent = candidate;
                        resolved = candidateEffect;
                    }
                    matchingEnteringIds.add(candidate.getId());
                }
            }
            if (enteringPermanent == null || resolved == null) continue;

            if (matchingEnteringIds.size() > 1 && boon.targetFilter() == null
                    && boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.ONE_OR_MORE_CREATURES_ENTER
                    && resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                // "Whenever a creature you control enters, [do something to] it": every creature that entered
                // together triggers the boon separately and consumes its own use.
                gameData.boons.remove(boon);
                int usesLeft = boon.remainingUses();
                for (UUID matchingId : matchingEnteringIds) {
                    if (usesLeft <= 0) break;
                    Permanent matching = gameQueryService.findPermanentById(gameData, matchingId);
                    if (matching == null) continue;
                    usesLeft--;
                    StackEntry perCreatureEntry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            boon.sourceCard(),
                            boon.controllerId(),
                            boon.sourceCard().getName() + "'s boon",
                            new ArrayList<>(List.of(resolved)));
                    perCreatureEntry.setTargetId(matching.getId());
                    perCreatureEntry.setTriggeringPermanentId(matching.getId());
                    perCreatureEntry.setTriggeringCardId(matching.getCard().getId());
                    perCreatureEntry.setNonTargeting(true);
                    gameData.enqueueTrigger(perCreatureEntry);
                    gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
                }
                if (usesLeft > 0) {
                    gameData.boons.add(new Boon(boon.controllerId(), boon.sourceCard(), boon.effect(),
                            usesLeft, boon.trigger(), boon.targetFilter()));
                }
                continue;
            }

            gameData.boons.remove(boon);
            if (boon.remainingUses() > 1) {
                gameData.boons.add(new Boon(boon.controllerId(), boon.sourceCard(), boon.effect(),
                        boon.remainingUses() - 1, boon.trigger(), boon.targetFilter()));
            }

            Permanent sourcePermanent = findPermanentByCard(gameData, boon.sourceCard());
            // "attached to one of them": when several creatures entered together the controller picks which.
            TargetFilter boonTargetFilter = boon.targetFilter();
            if (matchingEnteringIds.size() > 1
                    && resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    && (boonTargetFilter == null || boonTargetFilter instanceof PermanentPredicateTargetFilter)) {
                PermanentPredicate entered = new PermanentAnyOfPredicate(matchingEnteringIds.stream()
                        .<PermanentPredicate>map(PermanentIsSpecificPermanentPredicate::new).toList());
                boonTargetFilter = new PermanentPredicateTargetFilter(
                        boonTargetFilter instanceof PermanentPredicateTargetFilter existing
                                ? new PermanentAllOfPredicate(List.of(existing.predicate(), entered)) : entered,
                        "Target must be one of the creatures that entered");
            }
            if (boonTargetFilter != null
                    && resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                gameData.queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                        boon.sourceCard(), boon.controllerId(), new ArrayList<>(List.of(resolved)),
                        sourcePermanent == null ? null : sourcePermanent.getId(), enteringPermanent.getId(),
                        null, boonTargetFilter));
                gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
            } else {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        boon.sourceCard(),
                        boon.controllerId(),
                        boon.sourceCard().getName() + "'s boon",
                        new ArrayList<>(List.of(resolved)));
                entry.setTargetId(enteringPermanent.getId());
                entry.setTriggeringPermanentId(enteringPermanent.getId());
                entry.setTriggeringCardId(enteringPermanent.getCard().getId());
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
            }
        }
    }

    /** Collects creature-entry triggers from face-up planes, which are not battlefield permanents. */
    public void checkPlanarCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId,
                                                  Card enteringCreature) {
        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || !enteringPermanentHasType(gameData, enteringCreature, CardType.CREATURE)) {
            return;
        }

        UUID planarControllerId = gameData.planechase.controllerId;
        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
        UUID enteringPermanentId = enteringPermanent == null ? null : enteringPermanent.getId();
        TriggerContext.PermanentEnters context = new TriggerContext.PermanentEnters(
                enteringCreature, enteringCreatureControllerId, null, 1, enteringPermanentId);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (CardEffect effect : planarEnteringEffects(gameData, object.getCard(), planarControllerId,
                    enteringPermanent, ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)) {
                CardEffect resolved = unwrapTriggeringCardConditional(
                        effect, enteringCreature, gameData, planarControllerId);
                if (resolved == null) {
                    continue;
                }
                registry.dispatch(new TriggerMatchContext(gameData, null, planarControllerId, effect,
                                object.getCard(), object.copy()),
                        ZoneChangeWatch.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD, resolved, context);
            }
        }
    }

    /**
     * The effects of {@code plane}'s "whenever [an object] enters" abilities that one of {@code watches} collects and
     * whose trigger's filter {@code entering} matches as it entered (a plane isn't on the battlefield, so the trigger
     * pipeline doesn't match its abilities).
     */
    private List<CardEffect> planarEnteringEffects(GameData gameData, Card plane, UUID planarControllerId,
                                                   Permanent entering, ZoneChangeWatch... watches) {
        List<ZoneChangeWatch> watched = List.of(watches);
        FilterContext context = FilterContext.of(gameData).withSourceControllerId(planarControllerId)
                .withSourceCardId(plane.getId());
        List<CardEffect> effects = new ArrayList<>();
        for (TriggeredAbility ability : plane.getTriggeredAbilities()) {
            boolean matched = ability.conditions().stream().anyMatch(condition ->
                    condition.trigger() instanceof ZoneChangeTrigger trigger
                            && watches(watched, ZoneChangeWatch.of(ability, trigger, Zone.BATTLEFIELD))
                            && (trigger.filter() == null || entering != null && predicateEvaluationService
                            .matchesPermanentPredicate(entering, trigger.filter(), context)));
            if (matched) {
                ability.effects().forEach(registration -> effects.add(registration.effect()));
            }
        }
        return effects;
    }

    /**
     * Whether {@code watch} is one of {@code watched}. A plane's trigger condition that no zone-change watch collects
     * (Agyrem's "whenever a creature dies", which the creature-death collectors read) has none.
     */
    private static boolean watches(List<ZoneChangeWatch> watched, ZoneChangeWatch watch) {
        return watch != null && watched.contains(watch);
    }

    /** Collects ally-permanent-entry triggers from face-up planes, which are not battlefield permanents. */
    public void checkPlanarAllyPermanentEntersTriggers(GameData gameData, UUID enteringControllerId,
                                                        Card enteringCard) {
        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || !gameData.planechase.controllerId.equals(enteringControllerId)) {
            return;
        }

        UUID planarControllerId = gameData.planechase.controllerId;
        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCard);
        UUID enteringPermanentId = enteringPermanent == null ? null : enteringPermanent.getId();
        TriggerContext.PermanentEnters context = new TriggerContext.PermanentEnters(
                enteringCard, enteringControllerId, null,
                1 + gameQueryService.countETBExtraTriggers(
                        gameData, planarControllerId, enteringControllerId, enteringCard),
                enteringPermanentId);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            // Nyx's "a nontoken enchantment you control" narrows the any-permanent trigger to the enchantment watch's.
            for (CardEffect effect : planarEnteringEffects(gameData, object.getCard(), planarControllerId,
                    enteringPermanent, ZoneChangeWatch.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                    ZoneChangeWatch.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD)) {
                CardEffect resolved = unwrapTriggeringCardConditional(
                        effect, enteringCard, gameData, planarControllerId);
                if (resolved == null) {
                    continue;
                }
                registry.dispatch(new TriggerMatchContext(gameData, null, planarControllerId, effect,
                                object.getCard(), object.copy()),
                        ZoneChangeWatch.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD, resolved, context);
            }
        }
    }

    /**
     * Fires turn-scoped delayed triggers registered by Beck. Each registration is a separate
     * optional draw trigger, including for tokens and creatures controlled by the other player.
     */
    public void checkCreatureEntersThisTurnTriggers(GameData gameData, UUID enteringCreatureControllerId,
                                                    Card enteringCreature) {
        if (!enteringPermanentHasType(gameData, enteringCreature, CardType.CREATURE)) return;

        List<UUID> controllers = new ArrayList<>(gameData.orderedPlayerIds);
        int activePlayerIndex = controllers.indexOf(gameData.activePlayerId);
        if (activePlayerIndex > 0) {
            List<UUID> apnapControllers = new ArrayList<>(controllers.subList(activePlayerIndex, controllers.size()));
            apnapControllers.addAll(controllers.subList(0, activePlayerIndex));
            controllers = apnapControllers;
        }

        for (UUID controllerId : controllers) {
            List<Card> sources = gameData.creatureEntersDrawSourcesThisTurn.get(controllerId);
            if (sources != null) {
                for (Card sourceCard : new ArrayList<>(sources)) {
                    gameData.queueMayAbility(sourceCard, controllerId,
                            new MayEffect(new DrawCardEffect(), "Draw a card?"));
                    log.info("Game {} - {} triggers for creature entering (may draw)",
                            gameData.id, sourceCard.getName());
                }
            }

            if (controllerId.equals(enteringCreatureControllerId)) {
                List<Card> persistentSources = gameData.creatureEntersDrawSources.get(controllerId);
                if (persistentSources == null) continue;
                for (Card sourceCard : new ArrayList<>(persistentSources)) {
                    gameData.queueMayAbility(sourceCard, controllerId,
                            new MayEffect(new DrawCardEffect(), "Draw a card?"));
                    log.info("Game {} - {} triggers for controlled creature entering (may draw)",
                            gameData.id, sourceCard.getName());
                }
            }
        }
    }

    /**
     * The abilities a spell registered until end of turn that see a creature or an enchantment enter under their
     * controller's control.
     */
    public void checkAnyPermanentEntersTriggers(GameData gameData, UUID enteringControllerId, Card enteringCard) {
        if (!enteringPermanentHasType(gameData, enteringCard, CardType.CREATURE)
                && !enteringPermanentHasType(gameData, enteringCard, CardType.ENCHANTMENT)) {
            return;
        }
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(Triggers.enters(Triggers.CREATURE_OR_ENCHANTMENT,
                    ObjectRelation.YOU_CONTROL)) || !watcher.controllerId().equals(enteringControllerId)) {
                continue;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary creature-or-enchantment-enter trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /** The boons a noncreature permanent entering under {@code controllerId}'s control uses. */
    public void checkAllyPermanentEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCard);
        if (enteringPermanent != null && !gameQueryService.isCreature(gameData, enteringPermanent)) {
            collectAllyBoonTriggers(gameData, controllerId, List.of(enteringPermanent.getId()), false);
        }
    }

    /**
     * Puts a graveyard card's "whenever your commander enters" ability on the stack; the trigger's filter says whose
     * commander, whoever controls it.
     */
    private boolean queueGraveyardPermanentEntersTriggers(GameData gameData, Card card, UUID graveyardOwnerId,
                                                          List<CardEffect> effects, Permanent enteringPermanent) {
        boolean triggered = false;
        for (CardEffect resolved : effects) {
            if (resolved instanceof MayEffect may) {
                if (may.usesEnteringPermanentReference()) {
                    gameData.queueMayAbility(card, graveyardOwnerId, may, enteringPermanent.getId(), null);
                } else {
                    gameData.queueMayAbility(card, graveyardOwnerId, may);
                }
            } else {
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, graveyardOwnerId,
                        card.getName() + "'s ability", new ArrayList<>(List.of(resolved)));
                if (resolved.usesEnteringPermanentReference()) {
                    entry.setTargetId(enteringPermanent.getId());
                    entry.setNonTargeting(true);
                }
                gameData.enqueueTrigger(entry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
            triggered = true;
        }
        return triggered;
    }

    /** The abilities a spell registered until end of turn that see a creature enter under an opponent's control. */
    public void checkOpponentCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        if (!enteringPermanentHasType(gameData, enteringCreature, CardType.CREATURE)) return;

        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
        if (enteringPermanent == null) return;

        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watchesEntering(gameData, watcher, ZoneChangeWatch.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD,
                    enteringPermanent)
                    || watcher.controllerId().equals(enteringCreatureControllerId)) {
                continue;
            }

            CardEffect resolvedEffect = unwrapTriggeringCardConditional(
                    watcher.effect(), enteringCreature, gameData, watcher.controllerId());
            if (resolvedEffect == null) continue;

            enqueueTemporaryGlobalTrigger(gameData, watcher, resolvedEffect, enteringPermanent.getId(), 0);
        }
    }

    /** Handles "Whenever damage that would be dealt to you is prevented, ..." triggers. */
    public void checkControllerDamagePreventedTriggers(GameData gameData, UUID damagedPlayerId, int amount) {
        if (amount <= 0) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, null, null);
        for (Permanent perm : List.copyOf(damagedPlayerBattlefield)) {
            for (CardEffect effect : DamageWatch.ON_CONTROLLER_DAMAGE_PREVENTED.effectsOf(perm.getCard())) {
                var match = new TriggerMatchContext(gameData, perm, damagedPlayerId, effect);
                dispatch(match, DamageWatch.ON_CONTROLLER_DAMAGE_PREVENTED, effect, ctx);
            }
        }
    }

    /** Enqueues Judgment of Alexander's trigger for a creature-source damage event. */
    public void checkCommanderDamagePreventedTriggers(GameData gameData, UUID damagedPlayerId,
                                                       UUID preventedCreatureId, Card sourceCard) {
        if (damagedPlayerId == null || sourceCard == null) return;

        CardEffect effect = new CommanderCreaturesDealPowerDamageToPreventedCreatureEffect();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                damagedPlayerId,
                sourceCard.getName() + "'s triggered ability",
                List.of(effect),
                preventedCreatureId,
                (UUID) null);
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
    }

    /** The emblems' abilities that see an artifact enter under {@code controllerId}'s control. */
    public void checkAllyArtifactEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCard);
        if (enteringPermanent == null || !gameQueryService.isArtifact(gameData, enteringPermanent)) return;
        collectEmblemArtifactEntersTriggers(gameData, controllerId, enteringCard);
    }

    /** Fires Full Throttle's delayed untap trigger once for each registration at each combat. */
    public void checkUntapAttackedCreaturesEachCombatThisTurnTriggers(GameData gameData) {
        List<UUID> controllers = new ArrayList<>(gameData.orderedPlayerIds);
        int activePlayerIndex = controllers.indexOf(gameData.activePlayerId);
        if (activePlayerIndex > 0) {
            List<UUID> apnapControllers = new ArrayList<>(controllers.subList(activePlayerIndex, controllers.size()));
            apnapControllers.addAll(controllers.subList(0, activePlayerIndex));
            controllers = apnapControllers;
        }

        for (UUID controllerId : controllers) {
            List<Card> sources = gameData.untapAttackedCreaturesEachCombatThisTurnSources.get(controllerId);
            if (sources == null) continue;
            for (Card sourceCard : new ArrayList<>(sources)) {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        sourceCard,
                        controllerId,
                        sourceCard.getName() + "'s delayed ability",
                        List.of(new UntapPermanentsEffect(TapUntapScope.ATTACKED_CREATURES)));
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed combat trigger queued", gameData.id, sourceCard.getName());
            }
        }
    }

    private void collectEmblemArtifactEntersTriggers(GameData gameData, UUID controllerId,
                                                     Card enteringCard) {
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(controllerId)) continue;

            for (CardEffect effect : emblem.staticEffects()) {
                if (!(effect instanceof EmblemArtifactEntersTriggerEffect trigger)) {
                    continue;
                }

                Card source = emblem.sourceCard();
                Card sourceCard = source != null ? source : enteringCard;
                String description = (source != null ? source.getName() : "Emblem") + "'s emblem";
                gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                        sourceCard,
                        controllerId,
                        List.of(new DealDamageToAnyTargetEffect(trigger.damage()))
                ));
                gameLogService.append(gameData, GameLog.text(
                        description + " triggers for an artifact entering and deals "
                                + trigger.damage() + " damage to any target."));
                log.info("Game {} - {} triggers for {} entering (artifact-enter emblem)",
                        gameData.id, description, enteringCard.getName());
            }
        }
    }

    /** Fires emblem triggers for a combat-damage event dealt to a player. */
    public void checkEmblemCombatDamageTriggers(GameData gameData, UUID controllerId,
                                                UUID damagedPlayerId,
                                                Map<Permanent, Integer> combatDamageDealtToPlayer) {
        if (controllerId == null || damagedPlayerId == null || controllerId.equals(damagedPlayerId)
                || combatDamageDealtToPlayer == null
                || combatDamageDealtToPlayer.values().stream().noneMatch(damage -> damage > 0)) {
            return;
        }

        Card fallbackSource = combatDamageDealtToPlayer.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> entry.getKey().getCard())
                .findFirst()
                .orElse(null);

        for (Emblem emblem : gameData.emblems) {
            if (!controllerId.equals(emblem.controllerId())) continue;

            for (CardEffect effect : emblem.staticEffects()) {
                if (!(effect instanceof EmblemCombatDamageTriggerEffect trigger)) continue;
                if (gameData.lifeLostThisTurn.getOrDefault(damagedPlayerId, 0) >= trigger.lifeThreshold()) {
                    continue;
                }

                Card source = emblem.sourceCard() != null ? emblem.sourceCard() : fallbackSource;
                if (source == null) continue;

                String description = (emblem.sourceCard() != null ? emblem.sourceCard().getName() : "Emblem")
                        + "'s emblem";
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        source,
                        controllerId,
                        description,
                        List.of(trigger.triggeredEffect()));
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.text(
                        description + " triggers for combat damage dealt to an opponent."));
                log.info("Game {} - {} triggers for combat damage dealt to an opponent",
                        gameData.id, description);
            }
        }
    }

    /**
     * Fires token-entry abilities for a token-creation batch. Ordinary abilities use one trigger
     * for the batch, while abilities that branch on their resolution count receive one trigger for
     * each token in the batch.
     */
    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count) {
        checkAllyTokenEntersTriggers(gameData, controllerId, count, List.of());
    }

    private void collectTemporaryControllerSpellCastTriggers(GameData gameData, Card spellCard,
                                                               UUID castingPlayerId, Zone castZone,
                                                               UUID exiledSourcePermanentId) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL.trigger())
                    || !watcher.controllerId().equals(castingPlayerId)) {
                continue;
            }

            if (watcher.effect() instanceof TemporaryGlobalTriggerEffect trigger) {
                if (trigger.matches(castZone, exiledSourcePermanentId)) {
                    enqueueTemporaryGlobalTrigger(gameData, watcher, trigger.resolvedEffects(),
                            trigger.targetPlayerId());
                }
                continue;
            }

            if (!(watcher.effect() instanceof CopyControllerCastSpellOnSpellCastEffect trigger)) {
                if (watcher.targetFilter() != null) {
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            watcher.sourceCard(), watcher.controllerId(), List.of(watcher.effect()),
                            false, watcher.targetFilter(), 0, null, true));
                } else {
                    StackEntry triggerEntry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY, watcher.sourceCard(), watcher.controllerId(),
                            watcher.sourceCard().getName() + "'s ability",
                            new ArrayList<>(List.of(watcher.effect())));
                    triggerEntry.setNonTargeting(true);
                    gameData.stack.add(triggerEntry);
                }
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
                continue;
            }
            if (!predicateEvaluationService.matchesCardPredicate(
                    spellCard, trigger.spellFilter(), null, gameData, castingPlayerId)) {
                continue;
            }

            StackEntry spellEntry = gameData.stack.stream()
                    .filter(entry -> entry.getTargetableId().equals(spellCard.getId()))
                    .findFirst()
                    .orElse(null);
            if (spellEntry == null) {
                continue;
            }

            CardEffect copyEffect = new CopyControllerCastSpellEffect(
                    new StackEntry(spellEntry), castingPlayerId,
                    trigger.grantedKeywords(), trigger.additionalTypes(),
                    trigger.tokenCopy(), trigger.mayChooseNewTargets(), trigger.grantHasteToPermanentSpell(),
                    false, trigger.permanentSpellToken(), trigger.sacrificeAtEndStep());
            CardEffect resolutionEffect = trigger.manaCost() == null
                    ? copyEffect
                    : new MayPayManaEffect(trigger.manaCost(), copyEffect,
                    "Pay " + trigger.manaCost() + " to copy " + spellCard.getName() + "?");
            StackEntry triggerEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(resolutionEffect)));
            triggerEntry.setNonTargeting(true);
            gameData.stack.add(triggerEntry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    private void collectTemporaryGlobalSpellCastTriggers(GameData gameData, UUID castingPlayerId,
                                                          Zone castZone,
                                                          UUID exiledSourcePermanentId) {
        for (TemporaryGlobalTriggeredAbility watcher
                : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL.trigger())) {
                continue;
            }

            if (watcher.effect() instanceof TemporaryGlobalTriggerEffect trigger) {
                if (trigger.matches(castZone, exiledSourcePermanentId)
                        && trigger.matchesCastingPlayer(castingPlayerId)) {
                    enqueueTemporaryGlobalTrigger(gameData, watcher, trigger.resolvedEffects(),
                            trigger.targetPlayerId());
                }
                continue;
            }

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setTargetId(castingPlayerId);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary global spell-cast trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId,
                                              List<UUID> permanentIds) {
        checkAllyTokenEntersTriggers(gameData, controllerId, permanentIds.size(), permanentIds);
    }

    /** The boons the tokens entering under {@code controllerId}'s control use. */
    private void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count,
                                               List<UUID> permanentIds) {
        if (count <= 0) return;
        collectAllyBoonTriggers(gameData, controllerId, permanentIds, true);
    }

    private List<UUID> tokenIdAt(List<UUID> permanentIds, int index) {
        return index < permanentIds.size() ? List.of(permanentIds.get(index)) : List.of();
    }

    /**
     * "Whenever an Aura becomes attached to this creature" (ON_AURA_ATTACHED_TO_SELF, e.g. Brood
     * Keeper). Fires on the newly enchanted permanent for its own controller, regardless of who
     * controls the Aura. Call this after the Aura's {@code attachedTo} has been set.
     */
    public void checkAuraAttachedTriggers(GameData gameData, Card auraCard, UUID enchantedPermanentId) {
        if (auraCard == null || enchantedPermanentId == null) return;
        if (!auraCard.getSubtypes().contains(CardSubtype.AURA)) return;

        Permanent enchanted = gameQueryService.findPermanentById(gameData, enchantedPermanentId);
        if (enchanted == null) return;
        UUID controllerId = gameQueryService.findPermanentController(gameData, enchantedPermanentId);
        if (controllerId == null) return;

        for (CardEffect effect : MiscWatch.ON_AURA_ATTACHED_TO_SELF.effectsOf(enchanted.getCard())) {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    enchanted.getCard(),
                    controllerId,
                    enchanted.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    enchanted.getId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(enchanted.getCard()));
            log.info("Game {} - {} triggers on {} becoming attached to it",
                    gameData.id, enchanted.getCard().getName(), auraCard.getName());
        }
    }

    public void checkAuraAttachedTriggers(GameData gameData, Permanent aura, UUID enchantedPermanentId) {
        if (aura == null) return;
        Card auraCard = aura.getCard();
        checkAuraAttachedTriggers(gameData, auraCard, enchantedPermanentId);
        Permanent enchanted = gameQueryService.findPermanentById(gameData, enchantedPermanentId);
        if (enchanted == null) return;
        UUID enchantedControllerId = gameQueryService.findPermanentController(gameData, enchantedPermanentId);
        UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        if (auraControllerId != null && auraControllerId.equals(enchantedControllerId)
                && gameQueryService.isCreature(gameData, enchanted)) {
            List<Permanent> alliedBattlefield = gameData.playerBattlefields.get(auraControllerId);
            if (alliedBattlefield != null) {
                for (Permanent watcher : List.copyOf(alliedBattlefield)) {
                    if (gameQueryService.hasLostAllAbilities(gameData, watcher)) continue;
                    for (CardEffect effect :
                            MiscWatch.ON_ALLY_AURA_ATTACHED_TO_ALLY_CREATURE.effectsOf(watcher.getCard())) {
                        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                                watcher.getCard(), auraControllerId,
                                watcher.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(effect)), null, watcher.getId());
                        trigger.setNonTargeting(true);
                        gameData.enqueueTrigger(trigger);
                        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                    }
                }
            }
        }
        if (auraControllerId == null || auraControllerId.equals(enchantedControllerId)
                || gameQueryService.isLand(gameData, enchanted)
                || enchanted.getCard().getManaValue() > auraCard.getManaValue()) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(auraControllerId);
        if (battlefield == null) return;
        for (Permanent watcher : List.copyOf(battlefield)) {
            if (gameQueryService.hasLostAllAbilities(gameData, watcher)) continue;
            for (CardEffect effect :
                    MiscWatch.ON_ALLY_AURA_ATTACHED_TO_OPPONENT_NONLAND_PERMANENT.effectsOf(watcher.getCard())) {
                StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(), auraControllerId, watcher.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)), enchanted.getId(), aura.getId());
                trigger.setNonTargeting(true);
                gameData.enqueueTrigger(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
            }
        }
    }

    /** Fires an Equipment's targeted "becomes attached" abilities after a new attachment. */
    public void checkEquipmentAttachedTriggers(GameData gameData, Permanent equipment, UUID oldAttachedTo) {
        if (equipment == null || equipment.getAttachedTo() == null
                || equipment.getAttachedTo().equals(oldAttachedTo)
                || !equipment.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            return;
        }

        Permanent equipped = gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
        if (equipped == null || !gameQueryService.isCreature(gameData, equipped)) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, equipment.getId());
        if (controllerId == null) return;

        List<CardEffect> effects = MiscWatch.ON_EQUIPMENT_ATTACHED.effectsOf(equipment.getCard());
        if (effects == null || effects.isEmpty()) return;

        for (CardEffect authoredEffect : effects) {
            CardEffect effect = OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, equipment, authoredEffect);
            if (effect == null) continue;

            if (effect instanceof ChooseColorForSourceEffect) {
                // "As this Equipment becomes attached to a creature, choose a color" is not a triggered
                // ability, so the choice is made during the attachment rather than put on the stack.
                playerInputService.beginColorChoice(gameData, controllerId, equipment.getId(), null);
                continue;
            }

            if (effect instanceof SequenceEffect sequence && sequence.steps().size() == 2
                    && sequence.steps().get(0) instanceof ChooseCardNameAtResolutionEffect nameChoice
                    && sequence.steps().get(1) instanceof ChooseSubtypeForSourceEffect subtypeChoice) {
                // "As this Equipment becomes attached to a creature, choose a creature card name and a creature
                // type" is not a triggered ability, so both choices are made during the attachment.
                chooseCardNameHandler.begin(gameData, equipment.getCard(), controllerId, equipment.getId(),
                        nameChoice, subtypeChoice);
                continue;
            }

            if (effect instanceof AttachedCreatureBecomesCopyOfExiledCreatureEffect copy && copy.asAttaches()) {
                // "As this Equipment becomes attached to a creature, choose an exiled creature card ..." is not a
                // triggered ability, so the copy is made as the equipment attaches, without another stack resolution.
                StackEntry attachment = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        equipment.getCard(),
                        controllerId,
                        equipment.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        equipment.getId());
                attachment.setTriggeringPermanentId(equipped.getId());
                attachedCreatureCopyHandler.resolve(gameData, attachment, effect);
                continue;
            }

            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                        equipment.getCard(), controllerId, new ArrayList<>(List.of(effect)),
                        equipment.getId(), equipment.getCard().getTargetFilter(), equipped.getId()));
            } else {
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        equipment.getCard(),
                        controllerId,
                        equipment.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        equipment.getId());
                entry.setTriggeringPermanentId(equipped.getId());
                gameData.enqueueTrigger(entry);
            }
            OncePerTurnTriggerSupport.markIfNeeded(gameData, equipment, authoredEffect);
            gameLogService.append(gameData, GameLog.abilityTriggers(equipment.getCard()));
            log.info("Game {} - {} triggers when attached to {}", gameData.id,
                    equipment.getCard().getName(), equipped.getCard().getName());
        }

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
        }
    }

    private boolean hasWardAbility(GameData gameData, Permanent source) {
        if (source.isFaceDown()
                && (source.isCloaked() || source.isFaceDownWard())
                && gameQueryService.hasKeyword(gameData, source, Keyword.WARD)) {
            return true;
        }
        if (gameQueryService.isCreature(gameData, source)
                && (gameQueryService.hasKeyword(gameData, source, Keyword.WARD)
                || grantedTriggeredAbilitySupport.grantedTriggerEffects(
                gameData, source, TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL.trigger()).stream()
                .anyMatch(this::isCounterUnlessTrigger))) {
            return true;
        }
        if (source.isAttached() && source.getAttachedTo() != null) {
            Permanent attachedTo = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
            return attachedTo != null && (attachedTo.isCloaked()
                    || (gameQueryService.isCreature(gameData, attachedTo)
                    && gameQueryService.hasKeyword(gameData, attachedTo, Keyword.WARD)));
        }
        return false;
    }

    private boolean hasGrantedCounterUnlessTrigger(GameData gameData, Permanent source) {
        return grantedTriggeredAbilitySupport.grantedTriggerEffects(
                gameData, source, TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL.trigger()).stream()
                .anyMatch(this::isCounterUnlessTrigger);
    }

    private boolean isCounterUnlessTrigger(CardEffect effect) {
        if (effect instanceof CounterUnlessEffect) {
            return true;
        }
        return effect instanceof ConditionalEffect conditional
                && conditional.wrapped() instanceof CounterUnlessEffect;
    }

    /**
     * Collects abilities that trigger when the controller fully unlocks a Room. The caller must
     * invoke this only after the Room has actually transitioned from one unlocked door to both.
     */
    public void checkAllyRoomFullyUnlockedTriggers(GameData gameData, UUID controllerId, Permanent room) {
        if (controllerId == null || room == null
                || !room.getCard().hasType(CardType.ENCHANTMENT)
                || !room.getCard().getSubtypes().contains(CardSubtype.ROOM)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || battlefield.stream().noneMatch(
                permanent -> permanent.getId().equals(room.getId()))) {
            return;
        }

        TriggerContext context = new TriggerContext.RoomFullyUnlocked(room.getId());
        for (Permanent permanent : List.copyOf(battlefield)) {
            dispatchWatch(gameData, permanent, controllerId,
                    MiscWatch.ON_ALLY_ROOM_FULLY_UNLOCKED, context);
        }

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card card : new ArrayList<>(graveyard)) {
                queueGraveyardEerieTriggers(gameData, controllerId, card,
                        MiscWatch.GRAVEYARD_ON_ALLY_ROOM_FULLY_UNLOCKED, null);
            }
        }
    }

    /**
     * Collects abilities that trigger when a specified door of a Room becomes unlocked. This is
     * called for both the door chosen as a Room enters and a later door-unlocking action.
     */
    public void checkSelfRoomDoorUnlockedTriggers(GameData gameData, UUID controllerId,
                                                   Permanent room, int doorIndex) {
        if (controllerId == null || room == null || doorIndex < 0 || doorIndex > 1
                || !room.getCard().hasType(CardType.ENCHANTMENT)
                || !room.getCard().getSubtypes().contains(CardSubtype.ROOM)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || battlefield.stream().noneMatch(
                permanent -> permanent.getId().equals(room.getId()))) {
            return;
        }

        dispatchWatch(gameData, room, controllerId, MiscWatch.ON_SELF_ROOM_DOOR_UNLOCKED,
                new TriggerContext.RoomDoorUnlocked(room.getId(), doorIndex));
    }

    private boolean capturesTriggeringAuraManaValue(CardEffect effect) {
        if (effect instanceof SearchLibraryForAuraToBattlefieldAttachedToTargetCreatureEffect auraSearch) {
            return auraSearch.restrictToTriggeringAura();
        }
        if (effect instanceof MayEffect may) {
            return capturesTriggeringAuraManaValue(may.wrapped())
                    || capturesTriggeringAuraManaValue(may.elseEffect());
        }
        if (effect instanceof ConditionalEffect conditional) {
            return capturesTriggeringAuraManaValue(conditional.wrapped());
        }
        if (effect instanceof TriggeringCardConditionalEffect conditional) {
            return capturesTriggeringAuraManaValue(conditional.wrapped());
        }
        if (effect instanceof OncePerTurnTriggerEffect oncePerTurn) {
            return capturesTriggeringAuraManaValue(oncePerTurn.wrapped());
        }
        return false;
    }

    /**
     * "Whenever you play a land" (ON_CONTROLLER_PLAYS_LAND, e.g. Search the City, Juju Bubble) and
     * its opponent-side mirror ON_OPPONENT_PLAYS_LAND (Dirtcowl Wurm). Called from the land-play
     * sites only, so a land put onto the battlefield by an effect does not trigger either.
     * Dispatches through the collector registry so name-match gates (Search the City) and bare
     * effects (Juju Bubble) both resolve correctly.
     */
    public void checkControllerPlaysLandTriggers(GameData gameData, UUID playingPlayerId, Card landCard) {
        checkControllerPlaysLandTriggers(gameData, playingPlayerId, landCard, false);
    }

    public void checkControllerPlaysLandTriggers(GameData gameData, UUID playingPlayerId, Card landCard,
                                                 boolean fromExile) {
        checkControllerPlaysLandTriggers(gameData, playingPlayerId, landCard,
                fromExile ? Zone.EXILE : Zone.HAND);
    }

    public void checkControllerPlaysLandTriggers(GameData gameData, UUID playingPlayerId, Card landCard,
                                                 Zone playZone) {
        checkControllerPlaysLandTriggers(gameData, playingPlayerId, landCard, playZone, null);
    }

    public void checkControllerPlaysLandTriggers(GameData gameData, UUID playingPlayerId, Card landCard,
                                                 Zone playZone, UUID exiledSourcePermanentId) {
        var ctx = new TriggerContext.LandPlayed(playingPlayerId, landCard, playZone,
                exiledSourcePermanentId);
        if (playZone != Zone.HAND) {
            gameData.playersWhoPlayedOrCastFromOutsideHandThisTurn.add(playingPlayerId);
        }
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(playingPlayerId)) {
                dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND, ctx);
                if (!perm.getOriginalCard().getId().equals(landCard.getId())) {
                    dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_CONTROLLER_PLAYS_ANOTHER_LAND, ctx);
                }
            } else {
                dispatchWatch(gameData, perm, playerId, SpellAbilityWatch.ON_OPPONENT_PLAYS_LAND, ctx);
            }
        });
        if (gameData.planechase != null && gameData.planechase.controllerId != null) {
            SpellAbilityWatch watch = gameData.planechase.controllerId.equals(playingPlayerId)
                    ? SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND : SpellAbilityWatch.ON_OPPONENT_PLAYS_LAND;
            for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
                for (CardEffect effect : watch.effectsOf(object.getCard())) {
                    registry.dispatch(new TriggerMatchContext(gameData, null,
                                    gameData.planechase.controllerId, effect,
                                    object.getCard(), object.copy()),
                            watch, effect, ctx);
                }
            }
        }
        collectTemporaryControllerLandPlayTriggers(gameData, playingPlayerId, playZone, exiledSourcePermanentId);
    }

    private void collectTemporaryControllerLandPlayTriggers(GameData gameData, UUID playingPlayerId,
                                                            Zone playZone, UUID exiledSourcePermanentId) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(SpellAbilityWatch.ON_CONTROLLER_PLAYS_LAND.trigger())
                    || !watcher.controllerId().equals(playingPlayerId)
                    || !(watcher.effect() instanceof TemporaryGlobalTriggerEffect trigger)) {
                continue;
            }
            if (trigger.matches(playZone, exiledSourcePermanentId)) {
                enqueueTemporaryGlobalTrigger(gameData, watcher, trigger.resolvedEffects(),
                        trigger.targetPlayerId());
            }
        }
    }

    /** The emblems' landfall abilities and the exiled voyages that see a land enter under its controller's control. */
    public void checkAllyLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        int triggerCount = 1 + gameQueryService.countETBExtraTriggers(
                gameData, landControllerId, landControllerId, enteringLand);
        collectEmblemAllyLandEntersTriggers(gameData, landControllerId, enteringLand, triggerCount);
        collectExiledVoyageTriggers(gameData, landControllerId);
    }

    /** Collects land-entry triggers from face-up planes, which are not battlefield permanents. */
    public void checkPlanarLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        if (gameData.planechase == null || gameData.planechase.controllerId == null) return;

        UUID planarControllerId = gameData.planechase.controllerId;
        ZoneChangeWatch slot = planarControllerId.equals(landControllerId)
                ? ZoneChangeWatch.ON_ALLY_LAND_ENTERS_BATTLEFIELD
                : ZoneChangeWatch.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD;

        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            List<CardEffect> effects = slot.effectsOf(object.getCard());
            if (effects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    object.getCard(),
                    planarControllerId,
                    object.getCard().getName() + "'s ability",
                    new ArrayList<>(effects));
            entry.setTargetId(landControllerId);
            entry.setTriggeringPermanentControllerId(landControllerId);
            Permanent landPermanent = findPermanentByCard(gameData, enteringLand);
            if (landPermanent != null) entry.setTriggeringPermanentId(landPermanent.getId());
            entry.setSourcePlanarObject(object.copy());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(object.getCard()));
        }
    }

    /**
     * Binds the attackers matching an {@link AttackingCreaturesAwareEffect} at trigger creation, so the
     * effect acts on the creatures that attacked rather than on whatever is attacking at resolution.
     * Returns {@code null} when no attacking creature matches, meaning the trigger does not happen.
     */
    private CardEffect bindAttackingCreatures(GameData gameData, UUID attackingPlayerId, CardEffect effect) {
        if (!(effect instanceof AttackingCreaturesAwareEffect aware)) {
            return effect;
        }
        FilterContext context = FilterContext.of(gameData).withSourceControllerId(attackingPlayerId);
        List<AttackingPermanentSnapshot> attackers = gameData.playerBattlefields
                .getOrDefault(attackingPlayerId, List.of()).stream()
                .filter(Permanent::isAttacking)
                .filter(attacker -> predicateEvaluationService.matchesPermanentPredicate(
                        attacker, aware.attackingCreaturePredicate(), context))
                .map(attacker -> new AttackingPermanentSnapshot(
                        attacker.getId(), gameQueryService.getEffectivePower(gameData, attacker)))
                .toList();
        return attackers.isEmpty() ? null : aware.withAttackingCreatures(attackers);
    }

    /** Collects once-per-combat attack triggers from face-up planes. */
    public void checkPlanarAllyCreaturesAttackTriggers(GameData gameData, UUID attackingPlayerId,
                                                       int attackerCount) {
        if (attackerCount <= 0 || gameData.planechase == null
                || gameData.planechase.controllerId == null
                || !gameData.planechase.controllerId.equals(attackingPlayerId)) {
            return;
        }

        UUID planarControllerId = gameData.planechase.controllerId;
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            List<CardEffect> matchingEffects = new ArrayList<>();
            for (CardEffect effect : CombatWatch.ON_ALLY_CREATURES_ATTACK.effectsOf(object.getCard())) {
                if (effect instanceof ConditionalEffect conditional) {
                    boolean conditionMet = conditionEvaluationService.isMet(
                            gameData,
                            conditional.condition(),
                            ConditionContext.forCard(object.getCard(), planarControllerId)
                                    .withXValue(attackerCount));
                    if (conditional.interveningIf() && !conditionMet) {
                        continue;
                    }
                    if (conditional.condition() instanceof ExactlyAttackers) {
                        CardEffect bound = bindAttackingCreatures(gameData, attackingPlayerId, conditional.wrapped());
                        if (bound != null) {
                            matchingEffects.add(bound);
                        }
                    } else {
                        matchingEffects.add(effect);
                    }
                } else {
                    matchingEffects.add(effect);
                }
            }
            if (matchingEffects.isEmpty()) continue;

            if (matchingEffects.stream().anyMatch(effect -> effect.targetSpec().declaredTarget() != null)) {
                for (CardEffect effect : matchingEffects) {
                    int targetGroup = object.getCard().getEffectTargetIndex(effect);
                    TargetFilter filter = targetGroup >= 0
                            ? object.getCard().getTargetGroups().get(targetGroup).getFilter() : null;
                    gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            object.getCard(), planarControllerId, List.of(effect),
                            !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT), filter,
                            attackerCount, null, null, false, null, null, planarControllerId, object.copy()));
                }
                continue;
            }
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    object.getCard(),
                    planarControllerId,
                    object.getCard().getName() + "'s ability",
                    matchingEffects);
            entry.setXValue(attackerCount);
            entry.setSourcePlanarObject(object.copy());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(object.getCard()));
        }
    }

    /** Collects per-creature attack triggers from face-up planes. */
    public void checkPlanarAllyCreatureAttackTriggers(GameData gameData, Permanent attacker) {
        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || attacker == null || attacker.getAttackTarget() == null) {
            return;
        }

        UUID attackerControllerId = gameQueryService.findPermanentController(gameData, attacker.getId());
        UUID planarControllerId = gameData.planechase.controllerId;
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            List<CardEffect> matchingEffects = new ArrayList<>();
            List<CardEffect> attackEffects = new ArrayList<>(
                    CombatWatch.ON_ANY_CREATURE_ATTACKS.effectsOf(object.getCard()));
            if (planarControllerId.equals(attackerControllerId)) {
                attackEffects.addAll(CombatWatch.ON_ALLY_CREATURE_ATTACKS.effectsOf(object.getCard()));
            }
            for (CardEffect effect : attackEffects) {
                if (effect instanceof TriggeringCardConditionalEffect conditional) {
                    if (predicateEvaluationService.matchesCardPredicate(attacker.getCard(), conditional.predicate(),
                            object.getCard().getId(), gameData, planarControllerId)) {
                        matchingEffects.add(conditional.wrapped());
                    }
                } else if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                    FilterContext sourceContext = FilterContext.of(gameData)
                            .withSourceCardId(object.getCard().getId())
                            .withSourceControllerId(planarControllerId)
                            .withSourcePermanentId(attacker.getId());
                    if (predicateEvaluationService.matchesPermanentPredicate(
                            attacker, conditional.predicate(), sourceContext)) {
                        matchingEffects.add(conditional.wrapped());
                    }
                } else {
                    matchingEffects.add(effect);
                }
            }
            if (matchingEffects.isEmpty()) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    object.getCard(),
                    planarControllerId,
                    object.getCard().getName() + "'s ability",
                    matchingEffects,
                    attacker.getId(),
                    attacker.getId());
            entry.setAttackedTargetId(attacker.getAttackTarget());
            entry.setTriggeringPermanentId(attacker.getId());
            entry.setTriggeringPermanentControllerId(attackerControllerId);
            entry.setSourcePermanentSnapshot(new Permanent(attacker));
            entry.setSourcePlanarObject(object.copy());
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(object.getCard()));
        }
    }

    /** Collects ally-creature combat-damage triggers from face-up planes. */
    public void checkPlanarAllyCreatureCombatDamageToPlayerTriggers(
            GameData gameData, Permanent creature, UUID attackerId, UUID defenderId, int damageDealt,
            Set<UUID> firedBatchedSources) {
        if (damageDealt <= 0 || gameData.planechase == null || gameData.planechase.controllerId == null
                || creature == null || !gameData.planechase.controllerId.equals(attackerId)) {
            return;
        }

        UUID planarControllerId = gameData.planechase.controllerId;
        FilterContext triggerContext = FilterContext.of(gameData)
                .withSourceControllerId(planarControllerId);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (CardEffect effect : CombatWatch.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER.effectsOf(object.getCard())) {
                if (!(effect instanceof AllyCombatDamageTriggerEffect trigger)) {
                    continue;
                }
                if (trigger.dealerPredicate() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        creature, trigger.dealerPredicate(), triggerContext)) {
                    continue;
                }

                if (trigger.effect() instanceof ConditionalEffect conditional && conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forCard(object.getCard(), planarControllerId))) {
                    continue;
                }
                if (trigger.oncePerDamageStep() && !firedBatchedSources.add(object.getId())) {
                    continue;
                }
                UUID sourcePermanentId = trigger.bindSourceToDealer() ? creature.getId() : null;
                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        object.getCard(),
                        planarControllerId,
                        object.getCard().getName() + "'s triggered ability",
                        List.of(trigger.effect()),
                        defenderId,
                        sourcePermanentId);
                if (sourcePermanentId != null) {
                    entry.setSourcePermanentSnapshot(new Permanent(creature));
                }
                entry.setEventValue(damageDealt);
                entry.setSourcePlanarObject(object.copy());
                entry.setNonTargeting(true);
                gameData.enqueueTrigger(entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(object.getCard()));
            }
        }
    }

    private boolean hasPlanarPermanentDamageToYouTrigger(GameData gameData, UUID damagedPlayerId) {
        return gameData.planechase != null
                && Objects.equals(gameData.planechase.controllerId, damagedPlayerId)
                && gameData.planechase.faceUp.stream()
                .anyMatch(object -> !DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU.effectsOf(object.getCard()).isEmpty());
    }

    private void checkPlanarPermanentDealsDamageToYouTriggers(
            GameData gameData, UUID damagedPlayerId, Permanent damageSource, boolean isCombatDamage) {
        if (!gameQueryService.isCreature(gameData, damageSource)
                || gameData.planechase == null
                || !Objects.equals(gameData.planechase.controllerId, damagedPlayerId)) {
            return;
        }

        TriggerContext context = new TriggerContext.DamageToController(
                damagedPlayerId, damageSource.getId(), isCombatDamage);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (CardEffect effect : DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU.effectsOf(object.getCard())) {
                dispatchPlanarDamageTrigger(gameData, object,
                        DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect,
                        context, damageSource.getId(), damagedPlayerId);
            }
        }
    }

    private void checkPlanarAllySourceDealtDamageToOpponentTriggers(
            GameData gameData, UUID damagedPlayerId, UUID sourceControllerId,
            UUID sourcePermanentId, int amount) {
        if (gameData.planechase == null
                || !Objects.equals(gameData.planechase.controllerId, sourceControllerId)
                || sourcePermanentId == null) {
            return;
        }

        Permanent damageSource = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (damageSource == null || !gameQueryService.isCreature(gameData, damageSource)) return;

        TriggerContext context = new TriggerContext.DamageToControllerAmount(
                damagedPlayerId, amount, sourcePermanentId, sourceControllerId);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (CardEffect effect : DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT.effectsOf(object.getCard())) {
                dispatchPlanarDamageTrigger(gameData, object,
                        DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT, effect,
                        context, sourcePermanentId, sourceControllerId);
            }
        }
    }

    private void checkPlanarSourceDealtDamageToItsControllerTriggers(
            GameData gameData, UUID damagedPlayerId, UUID sourcePermanentId, int amount) {
        if (gameData.planechase == null
                || gameData.planechase.controllerId == null
                || Objects.equals(gameData.planechase.controllerId, damagedPlayerId)
                || sourcePermanentId == null) {
            return;
        }

        Permanent damageSource = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (damageSource == null || !gameQueryService.isCreature(gameData, damageSource)) return;

        TriggerContext context = new TriggerContext.DamageToControllerAmount(
                damagedPlayerId, amount, sourcePermanentId, damagedPlayerId);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            for (CardEffect effect : DamageWatch.ON_SOURCE_DEALS_DAMAGE_TO_ITS_CONTROLLER_PLANAR.effectsOf(object.getCard())) {
                dispatchPlanarDamageTrigger(gameData, object,
                        DamageWatch.ON_SOURCE_DEALS_DAMAGE_TO_ITS_CONTROLLER_PLANAR, effect,
                        context, sourcePermanentId, gameData.planechase.controllerId);
            }
        }
    }

    private boolean dispatchPlanarDamageTrigger(GameData gameData, PlanarObject object,
                                                DamageWatch watch, CardEffect authoredEffect,
                                                TriggerContext context, UUID triggeringPermanentId,
                                                UUID controllerId) {
        CardEffect effect = authoredEffect;
        boolean oncePerCreature = false;
        if (effect instanceof OncePerTurnPerCreatureTriggerEffect once) {
            Set<UUID> firedCreatures = gameData.oncePerCreatureTriggersFiredThisTurn
                    .computeIfAbsent(object.getId(), ignored -> ConcurrentHashMap.newKeySet());
            if (firedCreatures.contains(triggeringPermanentId)) return false;
            effect = once.wrapped();
            oncePerCreature = true;
        }

        boolean triggered = registry.dispatch(
                new TriggerMatchContext(gameData, null, controllerId, effect,
                        object.getCard(), object.copy()), watch, effect, context);
        if (triggered && oncePerCreature) {
            gameData.oncePerCreatureTriggersFiredThisTurn
                    .computeIfAbsent(object.getId(), ignored -> ConcurrentHashMap.newKeySet())
                    .add(triggeringPermanentId);
        }
        return triggered;
    }

    private void collectExiledVoyageTriggers(GameData gameData, UUID landControllerId) {
        for (var exiled : new ArrayList<>(gameData.exiledCards)) {
            UUID cardId = exiled.card().getId();
            UUID controllerId = gameData.exiledVoyageControllerIds.get(cardId);
            if (!gameData.exiledVoyageCounters.containsKey(cardId)) {
                continue;
            }
            if (gameData.findExiledCard(cardId) == null || controllerId == null) {
                gameData.exiledVoyageCounters.remove(cardId);
                gameData.exiledVoyageControllerIds.remove(cardId);
                continue;
            }
            if (!landControllerId.equals(controllerId)) {
                continue;
            }

            gameData.queueMayAbility(exiled.card(), controllerId, new MayEffect(
                    new PutVoyageCounterOnExiledCardEffect(),
                    "Put a voyage counter on Cosima?",
                    new ReturnVoyagingCardFromExileEffect()));
            gameLogService.append(gameData, GameLog.abilityTriggers(exiled.card()));
            log.info("Game {} - {} exiled voyage landfall trigger queued", gameData.id,
                    exiled.card().getName());
        }
    }

    private void collectEmblemAllyLandEntersTriggers(GameData gameData, UUID landControllerId,
                                                      Card enteringLand, int triggerCount) {
        for (Emblem emblem : gameData.emblems) {
            if (!emblem.controllerId().equals(landControllerId)) continue;

            for (CardEffect effect : emblem.staticEffects()) {
                Card source = emblem.sourceCard();
                Card sourceCard = source != null ? source : enteringLand;
                String description = (source != null ? source.getName() : "Emblem") + "'s emblem";
                if (effect instanceof DrawCardOnAllyLandEntersEffect) {
                    for (int i = 0; i < triggerCount; i++) {
                        gameData.queueMayAbility(sourceCard, emblem.controllerId(),
                                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
                        gameLogService.append(gameData, GameLog.text(description + " triggers."));
                        log.info("Game {} - {} landfall draw trigger queued", gameData.id, description);
                    }
                } else if (effect instanceof DealDamageToAnyTargetOnAllyLandEntersEffect damageTrigger
                        && enteringLandHasEffectiveSubtype(gameData, enteringLand, damageTrigger.landSubtype())) {
                    for (int i = 0; i < triggerCount; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                                sourceCard,
                                emblem.controllerId(),
                                new ArrayList<>(List.of(new DealDamageToAnyTargetEffect(damageTrigger.damage())))
                        ));
                        gameLogService.append(gameData,
                                GameLog.text(description + " triggers — choose a target for "
                                        + damageTrigger.damage() + " damage."));
                        log.info("Game {} - {} landfall damage trigger queued ({})",
                                gameData.id, description, damageTrigger.damage());
                    }
                }
            }
        }
    }

    /**
     * Dispatches an effect of a "whenever [an object] enters" ability with the limit {@code limit} to its watch's
     * handler. An ability limited to once each turn is one trigger per event, so a doubler (Panharmonicon) gives it no
     * second copy and nothing makes it trigger additional times; "do this only once each turn" marks the source as
     * its "you may" is accepted (CR 603.2h).
     */
    private boolean dispatchEntering(GameData gameData, Permanent perm, UUID controllerId, ZoneChangeWatch watch,
                                     CardEffect effect, TriggerLimit limit, TriggerContext.PermanentEnters ctx) {
        return dispatchEntering(gameData, perm, controllerId, watch, effect, effect, limit, ctx);
    }

    /** {@link #dispatchEntering} for {@code effect} unwrapped from the authored {@code rawEffect}. */
    private boolean dispatchEntering(GameData gameData, Permanent perm, UUID controllerId, ZoneChangeWatch watch,
                                     CardEffect rawEffect, CardEffect effect, TriggerLimit limit,
                                     TriggerContext.PermanentEnters ctx) {
        boolean limited = limit == TriggerLimit.ONCE_EACH_TURN || limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN;
        var effectCtx = limited ? new TriggerContext.PermanentEnters(ctx.enteringCard(), ctx.enteringControllerId(),
                ctx.defaultTargetPlayerId(), 1, ctx.mayPayTargetCardId()) : ctx;
        var match = new TriggerMatchContext(gameData, perm, controllerId, rawEffect,
                perm != null ? perm.getCard() : null, null, limit == TriggerLimit.DO_ONLY_ONCE_EACH_TURN, limited);
        return dispatch(match, watch, effect, effectCtx);
    }

    /**
     * Whether a landfall ability's trigger narrows the lands down beyond "a land" (Valakut, the Molten Pinnacle's "a
     * Mountain you control"), so a printed land with it sees its own entry.
     */
    private static boolean narrowedLandfall(TriggerCandidate candidate) {
        return candidate.ability().conditions().stream().anyMatch(condition ->
                condition.trigger() instanceof ZoneChangeTrigger trigger && trigger.filter() != null
                        && !trigger.filter().equals(Triggers.LAND));
    }

    private boolean dispatchEnter(GameData gameData, Permanent perm, UUID controllerId, Object slot,
                                  CardEffect effect, TriggerContext.PermanentEnters ctx) {
        var match = new TriggerMatchContext(gameData, perm, controllerId, effect);
        return dispatch(match, slot, effect, ctx);
    }

    private boolean dispatchEnter(GameData gameData, Card sourceCard, UUID controllerId, Object slot,
                                  CardEffect effect, TriggerContext.PermanentEnters ctx) {
        var match = new TriggerMatchContext(gameData, null, controllerId, effect, sourceCard);
        return dispatch(match, slot, effect, ctx);
    }

    private boolean dispatchEnter(GameData gameData, Permanent perm, UUID controllerId, Object slot,
                                  CardEffect rawEffect, CardEffect resolvedEffect,
                                  boolean markSourceOncePerTurnOnAcceptance,
                                  TriggerContext.PermanentEnters ctx) {
        var match = new TriggerMatchContext(gameData, perm, controllerId, rawEffect,
                perm != null ? perm.getCard() : null, markSourceOncePerTurnOnAcceptance);
        return dispatch(match, slot, resolvedEffect, ctx);
    }

    private boolean dispatchEnter(GameData gameData, Permanent perm, UUID controllerId, Object slot,
                                  CardEffect rawEffect, CardEffect resolvedEffect,
                                  TriggerContext.PermanentEnters ctx) {
        var match = new TriggerMatchContext(gameData, perm, controllerId, rawEffect);
        return dispatch(match, slot, resolvedEffect, ctx);
    }

    /**
     * Binds the dying creature card and the graveyard-entry version it just received into an
     * effect that later returns or otherwise acts on that card, so a card that left the graveyard
     * and came back is a new object the original trigger cannot act on (CR 400.7).
     */
    private static CardEffect bindDyingCard(GameData gameData, DyingCreatureCardAwareEffect aware, Card dyingCard) {
        return bindDyingCard(gameData, aware, dyingCard.getId());
    }

    private static CardEffect bindDyingCard(GameData gameData, DyingCreatureCardAwareEffect aware, UUID dyingCardId) {
        CardEffect bound = aware.boundToDyingCard(dyingCardId);
        if (bound instanceof DyingCreatureCardAwareEffect versioned) {
            return versioned.boundToDyingCardEntryVersion(gameData.graveyardEntryVersion(dyingCardId));
        }
        return bound;
    }

    private boolean dispatch(TriggerMatchContext match, Object slot, CardEffect effect,
                             TriggerContext context) {
        return dispatch(match, slot, effect, context, () -> registry.dispatch(match, slot, effect, context));
    }

    /**
     * {@link #dispatch} for an effect of a creature-death ability the trigger pipeline matched: the handler is
     * {@code watch}'s, not a slot's.
     */
    private boolean dispatchCreatureDeath(TriggerMatchContext match, CreatureDeathWatch watch, CardEffect effect,
                                          TriggerContext context) {
        return dispatch(match, null, effect, context,
                () -> deathTriggerCollectorService.dispatchCreatureDeath(watch, match, effect, context));
    }

    private boolean dispatch(TriggerMatchContext match, Object slot, CardEffect effect,
                             TriggerContext context, java.util.function.BooleanSupplier handler) {
        EffectSlot effectSlot = slot instanceof EffectSlot named ? named : null;
        if (context instanceof TriggerContext.PermanentEnters
                && gameQueryService.areOpponentPermanentETBTriggersSuppressed(
                match.gameData(), match.controllerId())) {
            return false;
        }
        List<CardEffect> grantedSpellEffects = effect instanceof SpellCastTriggerEffect spellCastTrigger
                ? spellCastTrigger.resolvedEffects()
                : effect instanceof NthSpellCastTriggerEffect nthSpellCastTrigger
                ? nthSpellCastTrigger.resolvedEffects()
                : List.of();
        boolean grantedSpellCascade = !grantedSpellEffects.isEmpty()
                && grantedSpellEffects.stream().allMatch(CascadeEffect.class::isInstance);
        // "This ability triggers only once each turn" caps the ability at one trigger a turn, so an
        // effect that makes it trigger additional times (Clara Oswald) cannot add a second copy.
        boolean triggersOnlyOnceEachTurn = match.triggersOnlyOnceEachTurn()
                || match.rawEffect() instanceof OncePerTurnTriggerEffect once
                && !once.firstTokenCreation() && !once.firstLifeGain()
                && !once.firstLandGraveyardEvent() && !once.firstLifeLoss() && !once.firstCycling();
        int previousCopies = match.gameData().beginTriggeredAbilityCopies(
                grantedSpellCascade || triggersOnlyOnceEachTurn ? 1 : 1 +
                gameQueryService.countAdditionalTriggeredAbilityTriggers(
                        match.gameData(), match.controllerId(), match.permanent(),
                        isDirectAttackTrigger(slot), context)
                + (context.causedByCreatureDying() && !effect.onlyTriggersOnSacrifice()
                ? gameQueryService.countAdditionalCreatureDeathTriggeredAbilityTriggers(
                        match.gameData(), match.controllerId(), match.permanent())
                : 0));
        GameData.CollectingTrigger previousCollecting = match.gameData().beginCollectingTrigger(
                new GameData.CollectingTrigger(effectSlot, effect, match.sourceCard()));
        Object activeBefore = match.gameData().interaction.activeInteraction();
        try {
            boolean handled = handler.getAsBoolean();
            Object activeAfter = match.gameData().interaction.activeInteraction();
            if (activeAfter != null && activeAfter != activeBefore) {
                // A handler that asks its question at once begins the prompt instead of queueing it.
                match.gameData().observeTrigger(RulesObserver.Route.INTERACTION, activeAfter);
            }
            return handled;
        } finally {
            match.gameData().endCollectingTrigger(previousCollecting);
            match.gameData().restoreTriggeredAbilityCopies(previousCopies);
        }
    }

    private boolean isDirectAttackTrigger(Object key) {
        if (!(key instanceof CombatWatch watch)) {
            return false;
        }
        return switch (watch) {
            case ON_ATTACK, ON_ALLY_CREATURES_ATTACK, ON_ALLY_CREATURES_ATTACK_PLAYER,
                    ON_ALLY_CREATURES_ATTACK_PLAYER_OR_PLANESWALKER,
                    ON_ALLY_CREATURE_ATTACKS,
                    ON_CREATURE_ATTACKS_YOU, ON_CREATURE_ATTACKS_YOU_DIRECTLY, ON_CREATURES_ATTACK_YOU,
                    ON_OPPONENT_CREATURES_ATTACK_YOU_UNBLOCKED,
                    ON_ANY_CREATURE_ATTACKS, ON_ANY_PLAYER_ATTACKS -> true;
            default -> false;
        };
    }

    private UUID instantOrSorceryCastOrCopyController(TriggerContext context) {
        Card spellCard = switch (context) {
            case TriggerContext.SpellCast spellCast -> spellCast.spellCard();
            case TriggerContext.SpellCopy spellCopy -> spellCopy.spellCard();
            default -> null;
        };
        if (spellCard == null
                || (!spellCard.hasType(CardType.INSTANT) && !spellCard.hasType(CardType.SORCERY))) {
            return null;
        }
        return switch (context) {
            case TriggerContext.SpellCast spellCast -> spellCast.castingPlayerId();
            case TriggerContext.SpellCopy spellCopy -> spellCopy.copyingPlayerId();
            default -> null;
        };
    }

    private void collectEvolveTrigger(GameData gameData, UUID controllerId, Permanent source,
                                      Permanent enteringPermanent, int triggerCount) {
        if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
            return;
        }
        int enteringPower = gameQueryService.getEffectivePower(gameData, enteringPermanent);
        int enteringToughness = gameQueryService.getEffectiveToughness(gameData, enteringPermanent);
        if (enteringPower <= gameQueryService.getEffectivePower(gameData, source)
                && enteringToughness <= gameQueryService.getEffectiveToughness(gameData, source)) {
            return;
        }

        for (int i = 0; i < triggerCount; i++) {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new EvolveTriggerEffect())),
                    0,
                    source.getId());
            entry.setTriggeringPermanentId(enteringPermanent.getId());
            entry.setTriggeringPermanentPowerAtTrigger(enteringPower);
            entry.setTriggeringPermanentToughnessAtTrigger(enteringToughness);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
            log.info("Game {} - {} triggers evolve for {} entering", gameData.id,
                    source.getCard().getName(), enteringPermanent.getCard().getName());
        }
    }

    private Permanent findPermanentByCard(GameData gameData, Card card) {
        Permanent[] found = new Permanent[1];
        gameData.forEachPermanent((playerId, permanent) -> {
            if (found[0] == null && permanent.getCard() == card) {
                found[0] = permanent;
            }
        });
        return found[0];
    }

    private Card effectiveEnteringCharacteristics(GameData gameData, Card card, Permanent permanent) {
        Card characteristics = card.createRuntimeCopy();
        if (permanent == null) return characteristics;
        Set<CardType> types = new HashSet<>();
        for (CardType type : CardType.values()) {
            boolean hasType = switch (type) {
                case CREATURE -> gameQueryService.isCreature(gameData, permanent);
                case ARTIFACT -> gameQueryService.isArtifact(gameData, permanent);
                case ENCHANTMENT -> gameQueryService.isEnchantment(gameData, permanent);
                case LAND -> gameQueryService.isLand(gameData, permanent);
                case PLANESWALKER -> gameQueryService.isPlaneswalker(gameData, permanent);
                case BATTLE -> gameQueryService.isBattle(gameData, permanent);
                default -> card.hasType(type);
            };
            if (hasType) types.add(type);
        }
        characteristics.setType(types.stream().findFirst().orElse(card.getType()));
        characteristics.setAdditionalTypes(types);
        characteristics.setSubtypes(new ArrayList<>(gameQueryService.getEffectiveSubtypes(gameData, permanent)));
        List<CardColor> colors = new ArrayList<>(gameQueryService.getEffectiveColors(gameData, permanent));
        characteristics.setColors(colors);
        characteristics.setColor(colors.stream().findFirst().orElse(null));
        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        for (Keyword keyword : Keyword.values()) {
            if (gameQueryService.hasKeyword(gameData, permanent, keyword)) keywords.add(keyword);
        }
        characteristics.setKeywords(keywords);
        return characteristics;
    }

    private boolean enteringLandHasEffectiveSubtype(GameData gameData, Card land, CardSubtype subtype) {
        Permanent permanent = findPermanentByCard(gameData, land);
        if (permanent == null) return land.getSubtypes().contains(subtype);
        return gameQueryService.getEffectiveSubtypes(gameData, permanent).contains(subtype);
    }

    private boolean enteringPermanentHasType(GameData gameData, Card card, CardType type) {
        Permanent permanent = findPermanentByCard(gameData, card);
        if (permanent == null) return card.hasType(type);
        return switch (type) {
            case CREATURE -> gameQueryService.isCreature(gameData, permanent);
            case ENCHANTMENT -> gameQueryService.isEnchantment(gameData, permanent);
            default -> card.hasType(type);
        };
    }

    /**
     * Gates Sigil Captain's "if that creature is 1/1", whose wrapper no trigger option replaces yet: its stats are read
     * with continuous effects applied as the creature entered (CR 613). Returns the wrapped effect if it fires,
     * {@code null} to skip, or the effect unchanged when it isn't wrapped.
     */
    private CardEffect unwrapEnterCreatureConditional(GameData gameData, Card enteringCreature,
                                                      Permanent source, CardEffect effect) {
        if (!(effect instanceof EnteringCreatureExactStatsConditionalEffect exactStats)) {
            return effect;
        }
        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
        if (enteringPermanent == null
                || gameQueryService.getEffectivePower(gameData, enteringPermanent) != exactStats.power()
                || gameQueryService.getEffectiveToughness(gameData, enteringPermanent) != exactStats.toughness()) {
            return null;
        }
        log.info("Game {} - {} triggers for {} entering ({})", gameData.id, source.getCard().getName(),
                enteringCreature.getName(), exactStats.triggerDescription(enteringCreature));
        return exactStats.wrapped();
    }

    private CardEffect unwrapOncePerTurnTrigger(GameData gameData, Permanent source, CardEffect effect) {
        return OncePerTurnTriggerSupport.unwrapIfAvailable(gameData, source, effect);
    }

    private boolean passesEnterInterveningIf(GameData gameData, Permanent source,
                                             UUID controllerId, Permanent enteringPermanent,
                                             CardEffect effect) {
        if (!(effect instanceof ConditionalEffect conditional) || !conditional.interveningIf()) {
            return true;
        }
        ConditionContext context = ConditionContext.forPermanent(source, controllerId);
        if (enteringPermanent != null) {
            context = context.withTriggeringPermanentId(enteringPermanent.getId())
                    .withTriggeringPermanentPowerAtTrigger(
                            gameQueryService.getEffectivePower(gameData, enteringPermanent));
        }
        return conditionEvaluationService.isMet(gameData, conditional.condition(),
                context);
    }

    private CardEffect unwrapLifeChangeConditional(GameData gameData, Permanent source,
                                                    UUID controllerId, CardEffect effect) {
        if (!(effect instanceof ConditionalEffect conditional)) {
            return effect;
        }
        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forPermanent(source, controllerId))) {
            return null;
        }
        return conditional.wrapped();
    }

    /**
     * Gates an enter trigger on the entering permanent's name matching the source's imprinted card
     * (Invader Parasite).
     */
    private CardEffect unwrapImprintedCardNameConditional(GameData gameData, Card enteringCard, Permanent source, CardEffect effect) {
        if (effect instanceof ConditionalEffect conditional
                && conditional.condition() instanceof ImprintedCardNameMatchesEnteringPermanent) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
            ConditionContext ctx = ConditionContext.forPermanent(source, controllerId).withTriggeringCard(enteringCard);
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(), ctx)) {
                return null;
            }
            return conditional.wrapped();
        }
        return effect;
    }

    /**
     * Gates an enter trigger on at least {@code minCount} permanents matching a predicate having
     * entered under {@code affectedPlayerId}'s control this turn (Landfall count).
     */
    private CardEffect unwrapPermanentEnteredThisTurnConditional(GameData gameData, UUID affectedPlayerId, CardEffect effect) {
        if (effect instanceof ConditionalEffect conditional
                && conditional.condition() instanceof PermanentEnteredThisTurn) {
            ConditionContext ctx = new ConditionContext(affectedPlayerId, null, null, null,
                    false, false, false, false, null, 0, null, null, false);
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(), ctx)) {
                return null;
            }
            return conditional.wrapped();
        }
        return effect;
    }

    // ── Internal dispatch ──────────────────────────────────────────────

    /**
     * Dispatches the abilities {@code watch} reads for each face-up scheme in the command zone whose owner passes
     * {@code ownerFilter}; a face-up scheme's triggered abilities trigger there (CR 314.4).
     */
    private void dispatchFaceUpSchemes(GameData gameData, TriggerWatch watch, TriggerContext ctx,
                                       Predicate<UUID> ownerFilter) {
        gameData.forEachFaceUpSchemeSource((ownerId, scheme) -> {
            if (ownerFilter.test(ownerId)) {
                dispatchWatch(gameData, scheme, ownerId, watch, ctx);
            }
        });
    }

    /**
     * "When you spend this mana to cast ..." riders on a mana ability are delayed triggers created when the
     * mana is produced (CR 106.6), so they still fire if the producing permanent has left the battlefield
     * by the time the mana is spent. Only those riders are collected from the departed source's
     * last-known information.
     */
    private void dispatchDepartedManaSourceSpellCastTriggers(GameData gameData, Card spellCard,
                                                              UUID castingPlayerId, TriggerContext ctx) {
        for (UUID sourceId : List.copyOf(gameData.spellCastManaSourceIds.getOrDefault(spellCard.getId(), Set.of()))) {
            if (gameQueryService.findPermanentById(gameData, sourceId) != null) continue;
            Permanent departed = gameData.departedPermanentSnapshots.values().stream()
                    .filter(snapshot -> snapshot.getId().equals(sourceId)).findFirst().orElse(null);
            if (departed == null) continue;
            for (EffectRegistration registration : SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL.registrationsOf(departed.getCard())) {
                CardEffect rider = registration.effect() instanceof MayEffect may ? may.wrapped() : registration.effect();
                if (rider instanceof SpellCastTriggerEffect trigger
                        && trigger.requiresManaProducedBySource()) {
                    dispatchSlotEffect(gameData, departed, castingPlayerId,
                            SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL, ctx, registration.effect());
                }
            }
        }
    }

    /**
     * Dispatches the abilities {@code watch} reads: the permanent's own abilities with its trigger, and those other
     * permanents grant it, each dispatched to {@code watch}'s handlers.
     */
    private boolean dispatchWatch(GameData gameData, Permanent perm, UUID controllerId, TriggerWatch watch,
                                  TriggerContext ctx) {
        return dispatchRegistrations(gameData, perm, controllerId, watch, ctx, null,
                watch.registrationsOf(perm.getCard()),
                staticBonus -> staticBonus == null
                        ? grantedTriggeredAbilitySupport.grantedTriggerEffects(gameData, perm, watch.trigger())
                        : grantedTriggeredAbilitySupport.grantedTriggerEffects(gameData, perm, staticBonus,
                                watch.trigger()));
    }

    /**
     * Dispatches {@code printedRegistrations}, then the effects {@code granted} returns for the permanent's static
     * bonus ({@code null} when that bonus may be stale), to {@code key}'s handlers.
     */
    private boolean dispatchRegistrations(GameData gameData, Permanent perm, UUID controllerId, Object key,
                                          TriggerContext ctx, Boolean oncePerBatchOnly,
                                          List<EffectRegistration> printedRegistrations,
                                          java.util.function.Function<GameQueryService.StaticBonus, List<CardEffect>> granted) {
        if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return false;
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
        if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()) return false;
        boolean triggered = false;
        // Reused for the granted-trigger lookup below unless a printed trigger was processed in
        // between and may have changed the game state.
        boolean staticBonusCurrent = true;
        if (!printedRegistrations.isEmpty() && !gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
            staticBonusCurrent = false;
            List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
            for (EffectRegistration registration : printedRegistrations) {
                if (oncePerBatchOnly != null
                        && (registration.triggerMode() == TriggerMode.ONCE_PER_BATCH) != oncePerBatchOnly) {
                    continue;
                }
                // Layer 3 (CR 612.1): the trigger condition is read from the permanent's
                // text-changed words (Glamerdye on Riverfall Mimic).
                CardEffect effect = TextChangeTransformer.transform(
                        registration.effect(), perm.getTextReplacements(), globalWordChange);
                if (dispatchSlotEffect(gameData, perm, controllerId, key, ctx, effect)) {
                    triggered = true;
                }
            }
        }
        // Triggered abilities granted continuously by another permanent (e.g. Pontiff of Blight
        // giving other creatures you control extort) fire off the permanent that has them.
        for (CardEffect effect : Boolean.TRUE.equals(oncePerBatchOnly) ? List.<CardEffect>of()
                : granted.apply(staticBonusCurrent ? staticBonus : null)) {
            if (dispatchSlotEffect(gameData, perm, controllerId, key, ctx, effect)) {
                triggered = true;
            }
        }
        return triggered;
    }

    private boolean dispatchSlotEffect(GameData gameData, Permanent perm, UUID controllerId,
            Object slot, TriggerContext ctx, CardEffect effect) {
        return dispatchSlotEffect(gameData, perm, controllerId, slot, ctx, effect, false);
    }

    /**
     * {@link #dispatchSlotEffect(GameData, Permanent, UUID, Object, TriggerContext, CardEffect)}, where
     * {@code markOnAcceptance} says whether taking the action uses up the ability's "do this only once each turn"
     * (CR 603.2h).
     */
    private boolean dispatchSlotEffect(GameData gameData, Permanent perm, UUID controllerId,
            Object slot, TriggerContext ctx, CardEffect effect, boolean markOnAcceptance) {
        CardEffect conditionedEffect = effect;
        if (slot == LibraryWatch.ON_CONTROLLER_DISCARD_EVENT
                && ctx instanceof TriggerContext.DiscardEvent discardEvent
                && effect instanceof TriggeringCardConditionalEffect conditional) {
            boolean matches = discardEvent.discardedCards().stream()
                    .anyMatch(card -> predicateEvaluationService.matchesCardPredicate(
                            card, conditional.predicate(), null, gameData, controllerId));
            if (!matches) {
                return false;
            }
            conditionedEffect = conditional.wrapped();
        }
        if (effect instanceof ConditionalEffect conditional
                && conditional.condition() instanceof SourceHasChosenMode) {
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forPermanent(perm, controllerId))) {
                return false;
            }
            conditionedEffect = conditional.wrapped();
        }
        if (slot == LibraryWatch.ON_CONTROLLER_DISCARD_EVENT
                && ctx instanceof TriggerContext.DiscardEvent firstDiscardEvent
                && conditionedEffect instanceof OncePerTurnTriggerEffect firstOnly
                && OncePerTurnTriggerEffect.FIRST_DISCARD_EVENT_KEY.equals(firstOnly.key())
                && gameData.cardsDiscardedThisTurn.getOrDefault(controllerId, 0)
                        != firstDiscardEvent.discardedCount()) {
            return false;
        }
        if (slot == LibraryWatch.ON_CONTROLLER_SURVEILS
                && ctx instanceof TriggerContext.Surveil surveil
                && !surveil.firstThisTurn()
                && conditionedEffect instanceof OncePerTurnTriggerEffect firstOnly
                && OncePerTurnTriggerEffect.FIRST_SURVEIL_KEY.equals(firstOnly.key())) {
            return false;
        }
        CardEffect toDispatch = unwrapOncePerTurnTrigger(gameData, perm, conditionedEffect);
        if (toDispatch == null) return false;

        boolean dispatched = dispatch(new TriggerMatchContext(gameData, perm, controllerId, effect,
                        perm != null ? perm.getCard() : null, markOnAcceptance), slot, toDispatch, ctx);
        if (dispatched && toDispatch != conditionedEffect
                && !(conditionedEffect instanceof OncePerTurnTriggerEffect once && once.markOnAcceptance())) {
            OncePerTurnTriggerSupport.markIfNeeded(gameData, perm, conditionedEffect);
        }
        return dispatched;
    }

    /**
     * Increment keyword. For each permanent the casting player controls with the {@link Keyword#INCREMENT}
     * keyword, fire the trigger if the mana spent on the cast spell is greater than that creature's current
     * power or toughness (the intervening-if; re-checked again at resolution per CR 603.4). The mana spent is
     * snapshotted into the stack entry's {@code xValue} for the resolution handler.
     */
    private void collectIncrementTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        int manaSpent = gameData.getSpellCastManaSpent(spellCard.getId());
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(castingPlayerId)) return;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return;
            if (!gameQueryService.hasKeyword(gameData, perm, Keyword.INCREMENT)) return;
            if (!gameQueryService.isCreature(gameData, perm)) return;
            if (manaSpent <= gameQueryService.getEffectivePower(gameData, perm)
                    && manaSpent <= gameQueryService.getEffectiveToughness(gameData, perm)) return;

            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    castingPlayerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new IncrementTriggerEffect())),
                    manaSpent,
                    perm.getId()
            ));
            log.info("Game {} - {} increment trigger queued (mana spent {})",
                    gameData.id, perm.getCard().getName(), manaSpent);
        });
    }

    /**
     * Unwraps triggering-card conditionals if present.
     * Returns the inner effect if the triggering card matches the predicate,
     * {@code null} if the condition is not met (caller should skip),
     * or the original effect unchanged if it wasn't wrapped.
     */
    CardEffect unwrapTriggeringCardConditional(CardEffect effect, Card triggeringCard,
                                               GameData gameData, UUID controllerId) {
        return unwrapTriggeringCardConditional(effect, triggeringCard, gameData, controllerId, null);
    }

    CardEffect unwrapTriggeringCardConditional(CardEffect effect, Card triggeringCard,
                                               GameData gameData, UUID controllerId,
                                               UUID sourceCardId) {
        if (effect instanceof TriggeringCardConditionalEffect conditional) {
            if (!predicateEvaluationService.matchesCardPredicate(triggeringCard, conditional.predicate(), sourceCardId,
                    gameData, controllerId)) {
                return null;
            }
            return conditional.wrapped();
        }
        return effect;
    }

    /**
     * Unwraps death-trigger conditionals that reference the dying creature's card or
     * on-battlefield characteristics (e.g. power/toughness).
     */
    CardEffect unwrapCreatureDeathConditional(CardEffect effect, Card dyingCard, Permanent dyingPermanent,
                                              GameData gameData, UUID controllerId) {
        return unwrapCreatureDeathConditional(effect, dyingCard, dyingPermanent, gameData, controllerId, null);
    }

    /**
     * Binds a "if this card is in your graveyard" death condition to the graveyard incarnation the
     * dying card has now, so a trigger collected for an earlier death cannot act on a later one (CR 400.7).
     */
    public CardEffect bindSourceCardInGraveyardVersion(GameData gameData, CardEffect effect, UUID dyingCardId) {
        if (effect instanceof ConditionalEffect conditional
                && conditional.condition() instanceof SourceCardInGraveyard) {
            return new ConditionalEffect(new SourceCardInGraveyard(gameData.graveyardEntryVersion(dyingCardId)),
                    conditional.wrapped(), conditional.interveningIf(), conditional.triggerTimeOnly());
        }
        if (effect instanceof MayPayManaEffect mayPay
                && mayPay.wrapped() instanceof ConditionalEffect inner
                && inner.condition() instanceof SourceCardInGraveyard) {
            return new MayPayManaEffect(mayPay.manaCost(), mayPay.dynamicManaCost(),
                    bindSourceCardInGraveyardVersion(gameData, inner, dyingCardId), mayPay.prompt(),
                    mayPay.payer(), mayPay.elseEffect(), mayPay.lifeCost(), mayPay.sourceIsTriggeringPermanent());
        }
        return effect;
    }

    private CardEffect unwrapCreatureDeathConditional(CardEffect effect, Card dyingCard, Permanent dyingPermanent,
                                                      GameData gameData, UUID controllerId, Permanent watcher) {
        return unwrapCreatureDeathConditional(effect, dyingCard, dyingPermanent, gameData, controllerId,
                watcher, null);
    }

    private CardEffect unwrapCreatureDeathConditional(CardEffect effect, Card dyingCard, Permanent dyingPermanent,
                                                      GameData gameData, UUID controllerId, Permanent watcher,
                                                      Boolean wasCreature) {
        return unwrapCreatureDeathConditional(effect, dyingCard, dyingPermanent, gameData, controllerId, watcher,
                wasCreature, false);
    }

    /**
     * Matches a subtype or color part of a death-trigger all-of against the dying permanent's last-known
     * characteristics, so an effect granted or changed before the creature died still counts. Returns
     * {@code null} for other parts, or when the snapshot has no last-known values for that part.
     */
    private static Boolean lastKnownPartMatch(Permanent dyingPermanent, CardPredicate part) {
        if (part instanceof CardSubtypePredicate subtype) {
            return dyingPermanent.getLastKnownSubtypes() == null ? null
                    : dyingPermanent.getLastKnownSubtypes().contains(subtype.subtype());
        }
        if (part instanceof CardNotPredicate not && not.predicate() instanceof CardSubtypePredicate subtype) {
            return dyingPermanent.getLastKnownSubtypes() == null ? null
                    : !dyingPermanent.getLastKnownSubtypes().contains(subtype.subtype());
        }
        if (part instanceof CardColorPredicate color) {
            return dyingPermanent.getLastKnownColors() == null ? null
                    : dyingPermanent.getLastKnownColors().contains(color.color());
        }
        if (part instanceof CardNotPredicate not && not.predicate() instanceof CardColorPredicate color) {
            return dyingPermanent.getLastKnownColors() == null ? null
                    : !dyingPermanent.getLastKnownColors().contains(color.color());
        }
        return null;
    }

    private CardEffect unwrapCreatureDeathConditional(CardEffect effect, Card dyingCard, Permanent dyingPermanent,
                                                      GameData gameData, UUID controllerId, Permanent watcher,
                                                      Boolean wasCreature, Boolean wasLand) {
        if (effect instanceof TriggeringCardConditionalEffect conditional) {
            if (conditional.predicate() instanceof CardTypePredicate typePredicate
                    && typePredicate.cardType() == CardType.LAND && Boolean.TRUE.equals(wasLand)) {
                // A permanent that was a land only through a continuous effect (Ashaya, Soul of the
                // Wild) still was a land as it died; its printed card does not show that.
                return conditional.wrapped();
            }
            if (conditional.predicate() instanceof CardTypePredicate typePredicate
                    && typePredicate.cardType() == CardType.CREATURE && Boolean.TRUE.equals(wasCreature)) {
                // A permanent that was a creature only through a continuous effect (an animated land)
                // still was a creature as it left; its printed card does not show that.
                return conditional.wrapped();
            }
            if (conditional.predicate() instanceof CardColorPredicate color
                    && dyingPermanent != null && dyingPermanent.getLastKnownColors() != null) {
                return dyingPermanent.getLastKnownColors().contains(color.color())
                        ? conditional.wrapped() : null;
            }
            if (conditional.predicate() instanceof CardIsMulticoloredPredicate
                    && dyingPermanent != null && dyingPermanent.getLastKnownColors() != null) {
                return dyingPermanent.getLastKnownColors().size() > 1 ? conditional.wrapped() : null;
            }
            if (conditional.predicate() instanceof CardSubtypePredicate subtype
                    && dyingPermanent != null && dyingPermanent.getLastKnownSubtypes() != null) {
                return dyingPermanent.getLastKnownSubtypes().contains(subtype.subtype())
                        ? conditional.wrapped() : null;
            }
            if (conditional.predicate() instanceof CardNotPredicate not
                    && not.predicate() instanceof CardSubtypePredicate subtype
                    && dyingPermanent != null && dyingPermanent.getLastKnownSubtypes() != null) {
                return dyingPermanent.getLastKnownSubtypes().contains(subtype.subtype())
                        ? null : conditional.wrapped();
            }
            if (conditional.predicate() instanceof CardAllOfPredicate allOf
                    && dyingPermanent != null
                    && allOf.predicates().stream().anyMatch(part -> lastKnownPartMatch(dyingPermanent, part) != null)) {
                UUID allOfSourceCardId = watcher == null ? null : watcher.getOriginalCard().getId();
                UUID allOfSourcePermanentId = watcher == null ? null : watcher.getId();
                for (CardPredicate part : allOf.predicates()) {
                    Boolean lastKnownMatch = lastKnownPartMatch(dyingPermanent, part);
                    boolean partMatches = lastKnownMatch != null ? lastKnownMatch
                            : predicateEvaluationService.matchesCardPredicate(dyingCard, part,
                                    allOfSourceCardId, gameData, controllerId, allOfSourcePermanentId, null, null,
                                    watcher);
                    if (!partMatches) {
                        return null;
                    }
                }
                return conditional.wrapped();
            }
            Permanent faceDownSnapshot = dyingPermanent == null ? null
                    : gameData.simultaneousDyingPermanents.getOrDefault(dyingPermanent.getId(), dyingPermanent);
            if (faceDownSnapshot != null && faceDownSnapshot.isFaceDown()) {
                // A face-down permanent has only its face-down characteristics (CR 708.2), so its
                // card types are the face-down ones rather than those printed on the card.
                Boolean faceDownMatch = matchesCardTypesOnly(
                        conditional.predicate(), faceDownSnapshot.getFaceDownCardTypes());
                if (faceDownMatch != null) {
                    return faceDownMatch ? conditional.wrapped() : null;
                }
            }
            UUID sourceCardId = watcher == null ? null : watcher.getOriginalCard().getId();
            UUID sourcePermanentId = watcher == null ? null : watcher.getId();
            // Card-type predicates read the dying permanent's last-known card, so an animated land
            // that was a creature when it died counts as a creature (CR 608.2h).
            boolean typeBased = conditional.predicate() instanceof CardTypePredicate
                    || conditional.predicate() instanceof CardAnyOfPredicate
                    || conditional.predicate() instanceof CardSupertypePredicate;
            Permanent typeSnapshot = dyingPermanent == null ? null
                    : gameData.simultaneousDyingPermanents.getOrDefault(dyingPermanent.getId(), dyingPermanent);
            Card typeCard = typeBased && typeSnapshot != null && typeSnapshot.getCard() != null
                    ? typeSnapshot.getCard() : dyingCard;
            Set<CardType> grantedTypes = typeSnapshot == null ? Set.of() : java.util.stream.Stream.concat(
                    typeSnapshot.getGrantedCardTypes().stream(),
                    typeSnapshot.getPersistentGrantedCardTypes().stream()).collect(java.util.stream.Collectors.toSet());
            if (!grantedTypes.isEmpty()) {
                // A type the permanent had only through an effect (Liquimetal Coating) still counts as it left.
                return matchesCardPredicateWithGrantedTypes(conditional.predicate(), typeCard, grantedTypes,
                        sourceCardId, gameData, controllerId, sourcePermanentId, watcher)
                        ? conditional.wrapped() : null;
            }
            if (!predicateEvaluationService.matchesCardPredicate(typeCard, conditional.predicate(),
                    sourceCardId, gameData, controllerId, sourcePermanentId, null, null, watcher)) {
                return null;
            }
            return conditional.wrapped();
        }
        if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
            Permanent perm = dyingPermanent != null ? dyingPermanent : new Permanent(dyingCard);
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceControllerId(controllerId);
            if (watcher != null) {
                filterContext = filterContext
                        .withSourceCardId(watcher.getOriginalCard().getId())
                        .withSourcePermanentSnapshot(watcher);
            } else if (dyingPermanent != null) {
                filterContext = filterContext
                        .withSourceCardId(dyingPermanent.getOriginalCard().getId())
                        .withSourcePermanentSnapshot(dyingPermanent)
                        .withSourcePermanentId(dyingPermanent.getId());
            }
            FilterContext matchContext = filterContext;
            DyingCreatureAttachmentSnapshot attachmentSnapshot = dyingPermanent == null ? null
                    : DyingCreatureAttachmentSnapshot.capture(gameData, dyingPermanent.getId());
            boolean matches = DyingCreatureAttachmentSnapshot.satisfiedAtDeath(attachmentSnapshot,
                    conditional.predicate(),
                    p -> {
                        // CR 603.10a: stat conditions look back at the creature as it last existed on the
                        // battlefield, including controller-dependent static bonuses that ended at death.
                        Integer lastKnownToughness = perm.getLastKnownToughness();
                        if (lastKnownToughness != null) {
                            int lastKnownPower = perm.getLastKnownPower() != null
                                    ? perm.getLastKnownPower() : perm.getEffectivePower();
                            Boolean statMatch = matchesDyingStats(p, lastKnownPower, lastKnownToughness);
                            if (statMatch != null) {
                                return statMatch;
                            }
                        }
                        return predicateEvaluationService.matchesPermanentPredicate(perm, p, matchContext);
                    });
            if (!matches) {
                return null;
            }
            return conditional.wrapped();
        }
        return effect;
    }

    private boolean matchesCardPredicateWithGrantedTypes(CardPredicate predicate, Card card,
                                                         Set<CardType> grantedTypes, UUID sourceCardId,
                                                         GameData gameData, UUID controllerId,
                                                         UUID sourcePermanentId, Permanent watcher) {
        if (predicate instanceof CardTypePredicate type) {
            return card.hasType(type.cardType()) || grantedTypes.contains(type.cardType());
        }
        if (predicate instanceof CardNotPredicate not) {
            return !matchesCardPredicateWithGrantedTypes(not.predicate(), card, grantedTypes, sourceCardId,
                    gameData, controllerId, sourcePermanentId, watcher);
        }
        if (predicate instanceof CardAllOfPredicate allOf) {
            return allOf.predicates().stream().allMatch(part -> matchesCardPredicateWithGrantedTypes(part, card,
                    grantedTypes, sourceCardId, gameData, controllerId, sourcePermanentId, watcher));
        }
        if (predicate instanceof CardAnyOfPredicate anyOf) {
            return anyOf.predicates().stream().anyMatch(part -> matchesCardPredicateWithGrantedTypes(part, card,
                    grantedTypes, sourceCardId, gameData, controllerId, sourcePermanentId, watcher));
        }
        return predicateEvaluationService.matchesCardPredicate(card, predicate,
                sourceCardId, gameData, controllerId, sourcePermanentId, null, null, watcher);
    }

    /**
     * Evaluates a predicate built only from card types against an explicit set of card types, or
     * returns {@code null} when the predicate looks at anything else.
     */
    private static Boolean matchesCardTypesOnly(CardPredicate predicate, Set<CardType> cardTypes) {
        if (predicate instanceof CardTypePredicate type) {
            return cardTypes.contains(type.cardType());
        }
        if (predicate instanceof CardNotPredicate not) {
            Boolean inner = matchesCardTypesOnly(not.predicate(), cardTypes);
            return inner == null ? null : !inner;
        }
        if (predicate instanceof CardAllOfPredicate allOf) {
            boolean result = true;
            for (CardPredicate part : allOf.predicates()) {
                Boolean partMatch = matchesCardTypesOnly(part, cardTypes);
                if (partMatch == null) return null;
                result &= partMatch;
            }
            return result;
        }
        if (predicate instanceof CardAnyOfPredicate anyOf) {
            boolean result = false;
            for (CardPredicate part : anyOf.predicates()) {
                Boolean partMatch = matchesCardTypesOnly(part, cardTypes);
                if (partMatch == null) return null;
                result |= partMatch;
            }
            return result;
        }
        return null;
    }

    /** Evaluates a power or toughness predicate against last-known stats, or null for any other predicate. */
    public static Boolean matchesDyingStats(PermanentPredicate predicate, int power, int toughness) {
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate p) {
            return toughness >= p.minToughness();
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate p) {
            return toughness <= p.maxToughness();
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate p) {
            return power >= p.minPower();
        }
        if (predicate instanceof com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate p) {
            return power <= p.maxPower();
        }
        return null;
    }

    private CardEffect bindDyingCreatureAttachments(CardEffect effect,
                                                    DyingCreatureAttachmentSnapshot snapshot) {
        if (snapshot == null || !(effect instanceof DyingCreatureAttachmentsAwareEffect aware)) {
            return effect;
        }
        return aware.boundToDyingCreatureAttachments(
                snapshot.auraCardIds(), snapshot.equipmentPermanentIds());
    }

    private boolean passesInterveningIf(GameData gameData, Permanent source, UUID controllerId,
                                        CardEffect effect) {
        return passesInterveningIf(gameData, source, controllerId, effect, null, null);
    }

    private boolean passesInterveningIf(GameData gameData, Permanent source, UUID controllerId,
                                        CardEffect effect, UUID triggeringPermanentId,
                                        Integer triggeringPermanentPowerAtTrigger) {
        if (!(effect instanceof ConditionalEffect conditional) || !conditional.interveningIf()) {
            return true;
        }
        return conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forPermanent(source, controllerId)
                        .withTriggeringPermanentId(triggeringPermanentId)
                        .withTriggeringPermanentPowerAtTrigger(triggeringPermanentPowerAtTrigger));
    }

    public void checkPlotTriggers(GameData gameData, UUID plottingPlayerId, Card plottedCard) {
        List<CardEffect> effects = ActionWatch.ON_SELF_BECOMES_PLOTTED.effectsOf(plottedCard);
        if (effects.isEmpty()) return;
        boolean needsAnyTarget = effects.stream().anyMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
        if (needsAnyTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.PlotTriggerAnyTarget(
                    plottedCard, plottingPlayerId, new ArrayList<>(effects)));
        } else {
            gameData.enqueueTrigger(new StackEntry(StackEntryType.TRIGGERED_ABILITY, plottedCard,
                    plottingPlayerId, plottedCard.getName() + "'s ability", new ArrayList<>(effects)));
        }
        gameLogService.append(gameData, GameLog.cardThen(plottedCard,
                " became plotted — its ability triggers!"));
    }

    public void processNextPlotTrigger(GameData gameData) {
        triggeredAbilityQueueService.processNextPlotTrigger(gameData);
    }

    public void checkBecomesCrewedTriggers(GameData gameData, Permanent crewedPermanent,
                                           UUID controllerId) {
        TriggerContext context = new TriggerContext.SelfBecomesCrewed(controllerId);
        for (CardEffect effect : MiscWatch.ON_SELF_BECOMES_CREWED.effectsOf(crewedPermanent.getCard())) {
            registry.dispatch(new TriggerMatchContext(gameData, crewedPermanent, controllerId, effect),
                    MiscWatch.ON_SELF_BECOMES_CREWED, effect, context);
        }
    }

    /** Collects abilities on a Spacecraft that trigger when a creature stations it. */
    public void checkStationedTriggers(GameData gameData, Permanent spacecraft,
                                       Permanent stationingCreature, UUID controllerId) {
        if (spacecraft == null || stationingCreature == null || controllerId == null) {
            return;
        }
        TriggerContext context = new TriggerContext.CreatureStationed(stationingCreature.getCard());
        for (CardEffect effect : MiscWatch.ON_SELF_BECOMES_STATIONED.effectsOf(spacecraft.getCard())) {
            registry.dispatch(new TriggerMatchContext(gameData, spacecraft, controllerId, effect),
                    MiscWatch.ON_SELF_BECOMES_STATIONED, effect, context);
        }
    }

    public void checkBecomesSaddledTriggers(GameData gameData, Permanent saddledPermanent,
                                             UUID controllerId) {
        TriggerContext context = new TriggerContext.SelfBecomesSaddled(controllerId);
        for (CardEffect effect : MiscWatch.ON_SELF_BECOMES_SADDLED.effectsOf(saddledPermanent.getCard())) {
            registry.dispatch(new TriggerMatchContext(gameData, saddledPermanent, controllerId, effect),
                    MiscWatch.ON_SELF_BECOMES_SADDLED, effect, context);
        }
    }

    /** Fires mutate triggers for all cards that currently make up the mutated permanent. */
    public void checkMutateTriggers(GameData gameData, Permanent mutatedPermanent,
                                    List<Card> mutatedCards, UUID controllerId) {
        mutatedPermanent.recordMutation();
        TriggerContext context = new TriggerContext.SelfMutates(controllerId);
        for (Card mutatedCard : mutatedCards) {
            for (CardEffect effect : ActionWatch.ON_SELF_MUTATES.effectsOf(mutatedCard)) {
                registry.dispatch(new TriggerMatchContext(gameData, mutatedPermanent, controllerId, effect,
                                mutatedCard),
                        ActionWatch.ON_SELF_MUTATES, effect, context);
            }
        }

        TriggerContext creatureMutatesContext = new TriggerContext.CreatureMutates(mutatedPermanent, controllerId);
        for (Card mutatedCard : mutatedCards) {
            for (CardEffect effect : ActionWatch.ON_ALLY_CREATURE_MUTATES.effectsOf(mutatedCard)) {
                registry.dispatch(new TriggerMatchContext(gameData, mutatedPermanent, controllerId, effect,
                                mutatedCard),
                        ActionWatch.ON_ALLY_CREATURE_MUTATES, effect, creatureMutatesContext);
            }
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent watcher : List.copyOf(battlefield)) {
                if (watcher.getId().equals(mutatedPermanent.getId())) continue;
                for (CardEffect effect : ActionWatch.ON_ALLY_CREATURE_MUTATES.effectsOf(watcher.getCard())) {
                    registry.dispatch(new TriggerMatchContext(gameData, watcher, controllerId, effect),
                            ActionWatch.ON_ALLY_CREATURE_MUTATES, effect, creatureMutatesContext);
                }
            }
        }
    }

    public void checkAllySourceDealtNoncombatDamageToCreatureTriggers(
            GameData gameData, UUID sourceControllerId, Permanent damagedCreature, int damageDealt) {
        if (sourceControllerId == null || damagedCreature == null || damageDealt <= 0) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;

        TriggerContext context = new TriggerContext.SourceDealsNoncombatDamageToCreature(
                damagedCreature, damageDealt, sourceControllerId);
        for (Permanent watcher : List.copyOf(battlefield)) {
            for (CardEffect effect : DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE.effectsOf(watcher.getCard())) {
                dispatch(new TriggerMatchContext(gameData, watcher, sourceControllerId, effect),
                        DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE, effect, context);
            }
        }
    }

    private int countCountersOnPermanent(Permanent permanent) {
        return snapshotCountersOnPermanent(permanent).values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    private Map<CounterType, Integer> snapshotCountersOnPermanent(Permanent permanent) {
        Map<CounterType, Integer> counters = new EnumMap<>(CounterType.class);
        for (CounterType type : CounterType.values()) {
            if (type == CounterType.ANY || type == CounterType.SILVER) {
                continue;
            }
            int count = permanent.getCounterCount(type);
            if (count > 0) {
                counters.put(type, count);
            }
        }
        return Map.copyOf(counters);
    }
}
